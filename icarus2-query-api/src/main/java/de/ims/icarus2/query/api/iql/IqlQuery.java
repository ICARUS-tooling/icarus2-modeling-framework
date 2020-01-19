/**
 *
 */
package de.ims.icarus2.query.api.iql;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ims.icarus2.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 *
 */
public class IqlQuery extends IqlUnique {
	/**
	 * Specifies the IQL dialect to be used. When not defined, the framework
	 * will default to {@value IqlConstants#DEFAULT_VERSION}.
	 */
	@JsonProperty(IqlProperties.DIALECT)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> dialect = Optional.empty();

	/**
	 * Import declarations for additional extensions or feature sets.
	 */
	@JsonProperty(IqlProperties.IMPORTS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlImport> imports = new ArrayList<>();

	/**
	 * Configuration of properties and/or switches.
	 */
	@JsonProperty(IqlProperties.SETUP)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlProperty> setup = new ArrayList<>();

	/**
	 * Basic definition of the corpora to be used for this query
	 */
	@JsonProperty(value=IqlProperties.CORPORA, required=true)
	private final List<IqlCorpus> corpora = new ArrayList<>();

	/**
	 * Vertical filtering via declaring a subset of the entire
	 * layer graph defined by the {@link #corpora} list.
	 */
	@JsonProperty(IqlProperties.LAYERS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlLayer> layers = new ArrayList<>();

	/**
	 * More fine-grained vertical filtering than using the
	 * {@link #layers} list. Takes priority over {@link #layers}
	 * when both are present.
	 */
	@JsonProperty(IqlProperties.SCOPES)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlScope> scopes = new ArrayList<>();

	//TODO add entries for pre and post processing of data (outside of result processing)

	/**
	 * The raw unprocessed query payload as provided by the user.
	 */
	@JsonProperty(value=IqlProperties.RAW_PAYLOAD, required=true)
	private String rawPayload;

	/**
	 * The processed query payload after being parsed by the
	 * query engine.
	 */
	@JsonProperty(IqlProperties.PROCESSED_PAYLOAD)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<IqlPayload> processedPayload = Optional.empty();

	/**
	 * The raw unprocessed grouping definitions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_GROUPING)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawGrouping = Optional.empty();

	/**
	 * The processed grouping definitions if {@link #rawGrouping}
	 * was set.
	 */
	@JsonProperty(IqlProperties.PROCESSED_GROUPING)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlGroup> processedGrouping = new ArrayList<>();

	/**
	 * The raw result processing instructions if provided.
	 */
	@JsonProperty(IqlProperties.RAW_RESULT_INSTRUCTIONS)
	@JsonInclude(Include.NON_ABSENT)
	private Optional<String> rawResultInstructions = Optional.empty();

	/**
	 * The processed result instructions if
	 * {@link #rawResultInstructions} was set.
	 */
	@JsonProperty(IqlProperties.PROCESSED_RESULT_INSTRUCTIONS)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlResultInstruction> processedResultInstructions = new ArrayList<>();

	/**
	 * Binary data required for the query. Each embedded data
	 * chunk is assigned a variable that can be used to access
	 * it from inside the query payload.
	 */
	@JsonProperty(IqlProperties.EMBEDDED_DATA)
	@JsonInclude(Include.NON_EMPTY)
	private final List<IqlData> embeddedData = new ArrayList<>();

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
		checkCollectionNotEmpty(corpora, "corpora");
		checkStringNotEmpty(rawPayload, "rawPayload");

		checkOptionalStringNotEmpty(dialect, "dialect");
		checkOptionalStringNotEmpty(rawGrouping, "rawGrouping");
		checkOptionalStringNotEmpty(rawResultInstructions, "rawResultInstructions");
		checkOptionalNested(processedPayload);
		checkCollection(imports);
		checkCollection(setup);
		checkCollection(layers);
		checkCollection(scopes);
		checkCollection(embeddedData);
		checkCollection(processedGrouping);
		checkCollection(processedResultInstructions);
	}

	public Optional<String> getDialect() { return dialect; }

	public List<IqlImport> getImports() { return CollectionUtils.unmodifiableListProxy(imports); }

	public List<IqlProperty> getSetup() { return CollectionUtils.unmodifiableListProxy(setup); }

	public List<IqlCorpus> getCorpora() { return CollectionUtils.unmodifiableListProxy(corpora); }

	public List<IqlLayer> getLayers() { return CollectionUtils.unmodifiableListProxy(layers); }

	public List<IqlScope> getScopes() { return CollectionUtils.unmodifiableListProxy(scopes); }

	public String getRawPayload() { return rawPayload; }

	public Optional<IqlPayload> getProcessedPayload() { return processedPayload; }

	public Optional<String> getRawGrouping() { return rawGrouping; }

	public List<IqlGroup> getProcessedGrouping() { return CollectionUtils.unmodifiableListProxy(processedGrouping); }

	public Optional<String> getRawResultInstructions() { return rawResultInstructions; }

	public List<IqlResultInstruction> getProcessedResultInstructions() { return CollectionUtils.unmodifiableListProxy(processedResultInstructions); }

	public List<IqlData> getEmbeddedData() { return CollectionUtils.unmodifiableListProxy(embeddedData); }

	public void setDialect(String dialect) { this.dialect = Optional.of(checkNotEmpty(dialect)); }

	public void setRawPayload(String rawPayload) { this.rawPayload = checkNotEmpty(rawPayload); }

	public void setProcessedPayload(IqlPayload processedPayload) { this.processedPayload = Optional.of(processedPayload); }

	public void setRawGrouping(String rawGrouping) { this.rawGrouping = Optional.of(checkNotEmpty(rawGrouping)); }

	public void setRawResultInstructions(String rawResultInstructions) { this.rawResultInstructions = Optional.of(checkNotEmpty(rawResultInstructions)); }

	public void addImport(IqlImport imp) { imports.add(requireNonNull(imp)); }

	public void addSetup(IqlProperty property) { setup.add(requireNonNull(property)); }

	public void addCorpus(IqlCorpus corpus) { corpora.add(requireNonNull(corpus)); }

	public void addLayer(IqlLayer layer) { layers.add(requireNonNull(layer)); }

	public void addScope(IqlScope scope) { scopes.add(requireNonNull(scope)); }

	public void addGrouping(IqlGroup group) { processedGrouping.add(requireNonNull(group)); }

	public void addResultInstruction(IqlResultInstruction instruction) { processedResultInstructions.add(requireNonNull(instruction)); }

	public void addEmbeddedData(IqlData data) { embeddedData.add(requireNonNull(data)); }

	//TODO add the 'forEach' style methods for all list properties

}
