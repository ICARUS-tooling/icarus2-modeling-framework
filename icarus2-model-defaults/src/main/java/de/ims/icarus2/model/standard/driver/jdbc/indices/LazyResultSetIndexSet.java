/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ims.icarus2.model.standard.driver.jdbc.indices;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.driver.indices.IndexSet;
import de.ims.icarus2.model.api.driver.indices.IndexValueType;
import de.ims.icarus2.model.api.driver.indices.standard.IndexBuffer;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements an {@code IndexSet} that is backed by a {@link ResultSet}.
 * The {@code ResultSet} is acquired lazily the first time a read method
 * on this index set is being called. By delaying the actual acquisition
 * of the result set it is possible to instantiate this index set with only
 * a query string.
 * <p>
 * Actual storage of the loaded index values is performed by an internal
 * {@link IndexBuffer} instance.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(IndexSet.class)
public class LazyResultSetIndexSet implements IndexSet {

	private volatile IndexBuffer buffer;

	protected final ResultSetSupplier resultSetSupplier;
	protected final int estimatedSize;

	protected final ToLongFunction<ResultSet> readFunc;

	public static LazyResultSetIndexSet forQuery(Statement stmt, String sql) {
		int bufferSize = getRequiredSize(stmt, sql);

		return forQuery(stmt, sql, bufferSize);
	}

	public static LazyResultSetIndexSet forQuery(final Statement stmt, String sql, int estimatedSize) {
		ResultSetSupplier supplier = () -> {
			// Make sure the statement gets closed properly
			stmt.closeOnCompletion();
			return stmt.executeQuery(sql);
		};

		return new LazyResultSetIndexSet(supplier, null, estimatedSize);
	}

	/**
	 * Assumes that the given {@code sql} query is of the form "SELECT ..."
	 * and creates a new query by wrapping the original one with
	 * "SELECT count(*) FROM (" and ")" to fetch the number of result rows.
	 * The returned table of size 1x1 is being read and the content of the
	 * single cell returned as required buffer size for hosting the content
	 * of the original query result.
	 *
	 * @param stmt
	 * @param sql
	 * @return
	 */
	protected static int getRequiredSize(Statement stmt, String sql) {
		String query = "SELECT count(*) FROM ("+sql+")";

		try (ResultSet rs = stmt.executeQuery(query)) {
			// Result has to be a 1x1 table
			rs.next();
			return rs.getInt(1);
		} catch(SQLException e) {
			throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Failed to fetch required size of result set: "+query, e);
		} finally {
			//TODO not sure if we should revert to former version of manual closing to prevent the SQLException from leaking
//			if(rs!=null) {
//				try {
//					rs.close();
//				} catch (SQLException e) {
//					throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Unable to close result set", e);
//				}
//			}
		}
	}

	/**
	 * Alternative interface to {@link Supplier} that allows direct exposure
	 * of SQL related exceptions.
	 *
	 * @author Markus Gärtner
	 *
	 */
	@FunctionalInterface
	public static interface ResultSetSupplier {

		/**
		 * Fetch the {@link ResultSet} this supplier is meant to provide.
		 * Note that this method will only ever be called once for the supplier
		 * instance provided to a {@link LazyResultSetIndexSet}!
		 * <p>
		 * The returned value must not be {@code null}!
		 *
		 * @return
		 * @throws SQLException
		 */
		public ResultSet get() throws SQLException;
	}

	public LazyResultSetIndexSet(ResultSetSupplier resultSetSupplier) {
		this(resultSetSupplier, -1);
	}

	public LazyResultSetIndexSet(ResultSetSupplier resultSetSupplier, int estimatedSize) {
		this(resultSetSupplier, null, estimatedSize);
	}

	public LazyResultSetIndexSet(ResultSetSupplier resultSetSupplier, ToLongFunction<ResultSet> readFunc, int estimatedSize) {
		requireNonNull(resultSetSupplier);

		this.resultSetSupplier = resultSetSupplier;
		this.readFunc = readFunc;
		this.estimatedSize = estimatedSize;
	}

	protected void readResultSet(ResultSet resultSet, IndexBuffer buffer) throws SQLException {

		// If special read function is present use it, otherwise assume long values in first column
		if(readFunc!=null) {
			while(resultSet.next()) {
				buffer.add(readFunc.applyAsLong(resultSet));
			}
		} else {
			while(resultSet.next()) {
				buffer.add(resultSet.getLong(1));
			}
		}
	}

	protected IndexBuffer buffer() {
		if(buffer==null) {
			synchronized (this) {
				if(buffer==null) {

					ResultSet resultSet = null;
					try {
						resultSet = resultSetSupplier.get();
					} catch (SQLException e) {
						throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Error while acquiring result set", e);
					}

					if(resultSet==null)
						throw new ModelException(GlobalErrorCode.DELEGATION_FAILED, "Returned result set is null");

					int size = estimatedSize;
					if(size==-1) {
						size = 100;
					}
					buffer = new IndexBuffer(size);

					try {
						readResultSet(resultSet, buffer);
					} catch (SQLException e) {
						throw new ModelException(GlobalErrorCode.INTERNAL_ERROR, "Error while reading indices from result set", e);
					}
				}
			}
		}

		return buffer;
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#size()
	 */
	@Override
	public int size() {
		return buffer().size();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#indexAt(int)
	 */
	@Override
	public long indexAt(int index) {
		return buffer().indexAt(index);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#firstIndex()
	 */
	@Override
	public long firstIndex() {
		return buffer().firstIndex();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#lastIndex()
	 */
	@Override
	public long lastIndex() {
		return buffer().lastIndex();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#getIndexValueType()
	 */
	@Override
	public IndexValueType getIndexValueType() {
		return buffer().getIndexValueType();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#isSorted()
	 */
	@Override
	public boolean isSorted() {
		return buffer().isSorted();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#sort()
	 */
	@Override
	public boolean sort() {
		return buffer().sort();
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#subSet(int, int)
	 */
	@Override
	public IndexSet subSet(int fromIndex, int toIndex) {
		return buffer().subSet(fromIndex, toIndex);
	}

	/**
	 * @see de.ims.icarus2.model.api.driver.indices.IndexSet#externalize()
	 */
	@Override
	public IndexSet externalize() {
		return buffer().externalize();
	}
}
