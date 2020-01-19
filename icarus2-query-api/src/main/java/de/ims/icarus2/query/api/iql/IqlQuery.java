/**
 *
 */
package de.ims.icarus2.query.api.iql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuery extends AbstractIqlQueryElement {

	/**
	 * Specifies the IQL dialect to be used. When not defined, the framework
	 * will default to {@value IqlConstants#DEFAULT_VERSION}.
	 */
	@JsonProperty(IqlProperties.DIALECT)
	public String dialect;

	/**
	 * Import declarations for additional extensions or feature sets.
	 */
	@JsonProperty(IqlProperties.IMPORTS)
	public List<IqlImport> imports = new ArrayList<>();

	/**
	 * Configuration of properties and/or switches.
	 */
	@JsonProperty(IqlProperties.SETUP)
	public List<IqlProperty> setup = new ArrayList<>();

	/**
	 * Basic definition of the corpora to be used for this query
	 */
	@JsonProperty(IqlProperties.CORPORA)
	public List<IqlCorpus> corpora = new ArrayList<>();

	/**
	 * Vertical filtering via declaring a subset of the entire
	 * layer graph defined by the {@link #corpora} list.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	public List<IqlLayer> layers = new ArrayList<>();

	/**
	 * More fine-grained vertical filtering than using the
	 * {@link #layers} list. Takes priority over {@link #layers}
	 * when both are present.
	 */
	@JsonProperty(IqlProperties.SCOPES)
	public List<IqlScope> scopes = new ArrayList<>();

	//TODO add entries for pre and post processing of data (outside of result processing)

	/**
	 * The raw unprocessed query payload as provided by the user.
	 */
	@JsonProperty(IqlProperties.RAW_PAYLOAD)
	public String rawPayload;

	/**
	 * The processed query payload after being parsed by the
	 * query engine.
	 */
	@JsonProperty(IqlProperties.PROCESSED_PAYLOAD)
	public IqlPayload processedPayload;

	/**
	 * The raw unprocessed grouping definitions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_GROUPING)
	public String rawGrouping;

	/**
	 * The processed grouping definitions if {@link #rawGrouping}
	 * was set.
	 */
	@JsonProperty(IqlProperties.PROCESSED_GROUPING)
	public List<IqlGroup> processedGrouping = new ArrayList<>();

	/**
	 * The raw result processing instructions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_RESULT_INSTRUCTIONS)
	public String rawResultInstructions;

	/**
	 * The processed result instructions if
	 * {@link #rawResultInstructions} was set.
	 */
	@JsonProperty(IqlProperties.PROCESSED_RESULT_INSTRUCTIONS)
	public List<IqlResultInstruction> processedResultInstructions = new ArrayList<>();

	/**
	 * Binary data required for the query. Each embedded data
	 * chunk is assigned a variable that can be used to access
	 * it from inside the query payload.
	 */
	@JsonProperty(IqlProperties.EMBEDDED_DATA)
	public List<IqlData> embeddedData = new ArrayList<>();

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
		return IqlType.QUERY;
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();

		//TODO
	}
}
