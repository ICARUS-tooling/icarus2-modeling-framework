/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.standard.highlight;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import de.ims.icarus2.model.api.highlight.Highlight;
import de.ims.icarus2.model.api.highlight.HighlightInfo;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.collections.set.DataSet;
import de.ims.icarus2.util.collections.set.DataSets;

/**
 * @author Markus Gärtner
 *
 */
public class HighlightBuilder {

	private DataSet<Item> items;
	private final List<Item> itemList = new ArrayList<>();

	private DataSet<HighlightInfo> highlightInfos;
	private final List<HighlightInfo> highlightInfoList = new ArrayList<>();

	private final EnumSet<HighlightBuilderOption> options;

	public HighlightBuilder(EnumSet<HighlightBuilderOption> options) {

		if(options==null) {
			options = EnumSet.noneOf(HighlightBuilderOption.class);
		}

		this.options = options;
	}

	public HighlightBuilder(HighlightBuilderOption...options) {

		EnumSet<HighlightBuilderOption> set = EnumSet.noneOf(HighlightBuilderOption.class);

		if(options!=null && options.length>0) {
			for(HighlightBuilderOption policy : options) {
				set.add(policy);
			}
		}

		this.options = set;
	}

	public HighlightBuilder() {
		this((HighlightBuilderOption[])null);
	}

	public HighlightBuilder items(DataSet<Item> items) {
		this.items = items;

		return this;
	}

	public HighlightBuilder addItem(Item item) {
		requireNonNull(item);

		itemList.add(item);

		return this;
	}

	public HighlightBuilder addItems(List<? extends Item> items) {
		itemList.addAll(items);

		return this;
	}

	public HighlightBuilder removeItem(Item item) {
		requireNonNull(item);

		itemList.remove(item);

		return this;
	}

	public HighlightBuilder removeItems(List<? extends Item> items) {
		itemList.removeAll(items);

		return this;
	}

	private DataSet<Item> buildItems() {
		DataSet<Item> result = items;

		if(result==null) {
			result = DataSets.createDataSet(itemList);
		}

		return result;
	}

	public HighlightBuilder clearRawItems() {
		itemList.clear();

		return this;
	}

	public HighlightBuilder fixItems() {
		items = buildItems();

		return this;
	}

	public HighlightBuilder highlightInfo(DataSet<HighlightInfo> highlightInfos) {
		this.highlightInfos = highlightInfos;

		return this;
	}

	public HighlightBuilder add(HighlightInfo highlightInfo) {
		requireNonNull(highlightInfo);

		highlightInfoList.add(highlightInfo);

		return this;
	}

	public HighlightBuilder addInfos(List<? extends HighlightInfo> highlightInfos) {
		highlightInfoList.addAll(highlightInfos);

		return this;
	}

	public HighlightBuilder removeInfo(HighlightInfo highlightInfo) {
		requireNonNull(highlightInfo);

		highlightInfoList.remove(highlightInfo);

		return this;
	}

	public HighlightBuilder removeInfos(List<? extends HighlightInfo> items) {
		highlightInfoList.removeAll(items);

		return this;
	}

	private DataSet<HighlightInfo> buildHighlightInfos() {
		DataSet<HighlightInfo> result = highlightInfos;

		if(result==null) {
			result = DataSets.createDataSet(highlightInfoList);
		}

		return result;
	}

	public HighlightBuilder clearRawHighlightInfos() {
		highlightInfoList.clear();

		return this;
	}

	public HighlightBuilder fixHighlightInfos() {
		highlightInfos = buildHighlightInfos();

		return this;
	}

	public void reset() {
		if(!options.contains(HighlightBuilderOption.RETAIN_ITEMS)) {
			items = null;
		}
		if(!options.contains(HighlightBuilderOption.RETAIN_HIGHLIGHT_INFOS)) {
			highlightInfos = null;
		}
		if(options.contains(HighlightBuilderOption.CLEAR_RAW_ITEMS)) {
			clearRawItems();
		}
		if(options.contains(HighlightBuilderOption.CLEAR_RAW_HIGHLIGHT_INFOS)) {
			clearRawHighlightInfos();
		}
	}

	public Highlight build() {
		return build(buildItems(), buildHighlightInfos());
	}

	public Highlight build(DataSet<Item> items) {
		return build(items, buildHighlightInfos());
	}

	public Highlight build(DataSet<Item> items, DataSet<HighlightInfo> highlightInfos) {
		Highlight result = new SimpleHighlight(items, highlightInfos);

		reset();

		return result;
	}

	public enum HighlightBuilderOption {
		RETAIN_ITEMS,
		CLEAR_RAW_ITEMS,
		RETAIN_HIGHLIGHT_INFOS,
		CLEAR_RAW_HIGHLIGHT_INFOS,
		;
	}
}
