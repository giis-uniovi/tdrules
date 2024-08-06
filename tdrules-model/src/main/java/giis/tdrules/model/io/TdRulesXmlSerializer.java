package giis.tdrules.model.io;

import static giis.tdrules.model.ModelUtil.safe;

import java.util.ArrayList;
import java.util.List;

import giis.portable.xml.tiny.XNode;
import giis.tdrules.model.RuleTypes;
import giis.tdrules.openapi.model.QueryEntitiesBody;
import giis.tdrules.openapi.model.QueryParam;
import giis.tdrules.openapi.model.QueryParametersBody;
import giis.tdrules.openapi.model.RunParams;
import giis.tdrules.openapi.model.TdRule;
import giis.tdrules.openapi.model.TdRules;
import giis.tdrules.openapi.model.TdRulesBody;

/**
 * Custom xml serialization/deserialization of a rules model
 * 
 * Model is v4, but xml still reads and writes using the old v3 api notation.
 */
public class TdRulesXmlSerializer extends BaseXmlSerializer {

	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private static final String VERSION = "version";
	private static final String DEVELOPMENT = "development";
	private static final String PARSEDSQL = "parsedsql";
	private static final String PARAMETERS = "parameters";
	private static final String ERROR = "error";
	
	public TdRules deserialize(String xml) {
		XNode xtdrules=new XNode(xml);
		TdRules tdrules=new TdRules();
		String rulesClass=RuleTypes.normalizeV4(xtdrules.name());

		tdrules.setRulesClass(rulesClass);
		tdrules.setVersion(getElemAttribute(xtdrules, VERSION));
		if (xtdrules.getChild(VERSION) != null)
			tdrules.setEnvironment(xtdrules.getChild(VERSION).getChild(DEVELOPMENT) == null ? "" : DEVELOPMENT);
		for (String attr : getExtendedAttributeNames(xtdrules, new String[] {}))
			tdrules.putSummaryItem(attr, xtdrules.getAttribute(attr));

		tdrules.setQuery(getElemAttribute(xtdrules, "sql"));
		tdrules.setParsedquery(getElemAttribute(xtdrules, PARSEDSQL));
		if (xtdrules.getChild(PARAMETERS) != null)
			for (XNode attrnode: safe(xtdrules.getChild(PARAMETERS).getChildren("runParams")))
				tdrules.addParametersItem(deserializeRunParams(attrnode));
		tdrules.setError(getElemAttribute(xtdrules, ERROR));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		XNode xrules=xtdrules.getChild(ruleTag+"s");
		if (xrules==null)
			return tdrules;
		addDeserializedRules(xrules, ruleTag, tdrules);
		return tdrules;
	}
	private void addDeserializedRules(XNode xrules, String ruleTag, TdRules tdrules) {
		for (XNode rnode : xrules.getChildren(ruleTag)) {
			TdRule rule=new TdRule();
			for (String attr : getExtendedAttributeNames(rnode, new String[] {}))
				rule.putSummaryItem(attr, rnode.getAttribute(attr));
			rule.setId(getElemAttribute(rnode, "id"));
			rule.setCategory(getElemAttribute(rnode, "category"));
			rule.setMaintype(getElemAttribute(rnode, "type"));
			rule.setSubtype(getElemAttribute(rnode, "subtype"));
			rule.setLocation(getElemAttribute(rnode, "location"));
			rule.setEquivalent(rnode.getChild("equivalent")==null ? "" : "true");
			rule.setQuery(getElemAttribute(rnode, "sql"));
			rule.setDescription(getElemAttribute(rnode, "description"));
			if (rnode.getChild(PARAMETERS) != null)
				for (XNode attrnode: safe(rnode.getChild(PARAMETERS).getChildren("runParams")))
					rule.addParametersItem(deserializeRunParams(attrnode));
			rule.setError(getElemAttribute(rnode, ERROR));
			tdrules.addRulesItem(rule);
		}
	}
	private RunParams deserializeRunParams(XNode node) {
		RunParams params = new RunParams();
		params.setWhen(getElemAttribute(node, "when"));
		params.setResult(getElemAttribute(node, "result"));
		params.setParams(deserializeQueryParamList(node));
		return params;
	}
	public List<QueryParam> deserializeQueryParamList(XNode paramNode) {
		List<QueryParam> params = new ArrayList<>();
		for (XNode xparam : safe(paramNode.getChildren("parameter"))) {
		    QueryParam param=new QueryParam();
		    param.setName(xparam.getAttribute("name"));
		    param.setValue(xparam.getAttribute("value"));
			params.add(param);
		}
		return params;
	}
	private String rulesClassToRuleTag(String rulesClass) {
		return RuleTypes.FPC.equals(RuleTypes.normalizeV4(rulesClass)) ? "fpcrule" : "mutant";
	}
	
	public String serialize(TdRules sqr) {
		StringBuilder sb=new StringBuilder();
		String rulesClass=sqr.getRulesClass();
		sb.append(XML_HEADER)
			.append("\n<" + RuleTypes.normalizeV3(rulesClass))
			.append(setExtendedAttributes(sqr.getSummary()))
			.append(">");
		sb.append("\n<version>")
			.append(sqr.getVersion())
			.append(DEVELOPMENT.equals(sqr.getEnvironment()) ? "<development/>" : "")
			.append("</version>");
		sb.append(setElemAttribute(0, "sql", sqr.getQuery()))
			.append(setElemAttribute(0, PARSEDSQL, sqr.getParsedquery()));
		sb.append(serializeRunParams(sqr.getParameters()));
		sb.append(setElemAttribute(0, ERROR, sqr.getError()));
		
		String ruleTag=rulesClassToRuleTag(rulesClass);
		if ("".equals(sqr.getError())) { //no muestra tag si ha habido error generando
			sb.append("\n<" + ruleTag + "s>");
			for (TdRule rule : safe(sqr.getRules()))
				sb.append("\n").append(serialize(rule, ruleTag));
			sb.append("\n</" + ruleTag + "s>");
		}
		sb.append("\n</" + RuleTypes.normalizeV3(rulesClass) + ">");
		return sb.toString();
	}
	public String serialize(TdRule rule, String ruleTag) {
		StringBuilder sb=new StringBuilder();
		sb.append("  <" + ruleTag)
			.append(setExtendedAttributes(rule.getSummary()))
			.append(">");
		sb.append(setElemAttribute("id", rule.getId()))
			.append(setElemAttribute("category", rule.getCategory()))
			.append(setElemAttribute("type", rule.getMaintype()))
			.append(setElemAttribute("subtype", rule.getSubtype()))
			.append(setElemAttribute("location", rule.getLocation()))
			.append("true".equals(rule.getEquivalent()) ? "\n    <equivalent/>" : "")
			.append(setElemAttribute(4, "sql", rule.getQuery()))
			.append(setElemAttribute(4, "description", rule.getDescription()))
			.append(serializeRunParams(rule.getParameters()))
			.append(setElemAttribute(4, ERROR, rule.getError()))
			.append("\n  </" + ruleTag + ">");
		return sb.toString();
	}
	private String serializeRunParams(List<RunParams> param) {
		if (safe(param).size() == 0) // NOSONAR for net compatibility
			return "";
		StringBuilder sb=new StringBuilder();
		sb.append("\n<parameters>");
		for (RunParams item : safe(param))
			sb.append("\n  <runParams>")
			.append(setElemAttribute("when", item.getWhen()))
			.append(setElemAttribute("result", item.getResult()))
			.append("\n    ")
			.append(serializeQueryParamList(item.getParams(), false))
			.append("\n  </runParams>");
		sb.append("\n</parameters>");
		return sb.toString();
	}
	private String serializeQueryParamList(List<QueryParam> params, boolean breakLines) {
		StringBuilder sb=new StringBuilder();
    	for (QueryParam param : safe(params))
    		sb.append(breakLines ? "\n" : "")
    			.append("<parameter")
				.append(setAttribute("name", param.getName()))
				.append(setAttribute("value", param.getValue()))
				.append(" />");
    	return sb.toString();
	}
	
	//Deserializacion de otros objetos obtenidos de los servicios TdRules
	//Aunque en v2 se devuelve una estructura similar a las reglas que incluye version, entorno, etc.
	//para la v3 y 4 se simplificara, y solo se tiene el error (uno solo) o el contenido objeto a devolver
	
	public QueryEntitiesBody deserializeEntities(String xml) {
		XNode xentities=new XNode(xml);
		QueryEntitiesBody entities=new QueryEntitiesBody();
		entities.setError(getElemAttribute(xentities, ERROR));
		for (XNode xentity : safe(xentities.getChildren("table")))
			entities.addEntitiesItem(XNode.decodeText(xentity.innerText()));
		return entities;
	}
	public QueryParametersBody deserializeQueryParamList(String xml) {
		XNode xparams=new XNode(xml);
		QueryParametersBody sparams=new QueryParametersBody();
		sparams.setError(getElemAttribute(xparams, ERROR));
		XNode paramNode=xparams.getChild(PARAMETERS);
		if (paramNode==null)
			return sparams;
		sparams.setParameters(deserializeQueryParamList(paramNode));
		return sparams;
	}
	public String serialize(QueryEntitiesBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqltables>");
		if ("".equals(model.getError()))
			for (String entity : safe(model.getEntities()))
				sb.append(setElemAttribute(0, "table", entity));
		else
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		sb.append("\n</sqltables>");
		return sb.toString();
	}
	public String serialize(QueryParametersBody model) {
		StringBuilder sb=new StringBuilder();
		sb.append(XML_HEADER)
			.append("\n<sqlparameters>");
		if ("".equals(model.getError())) {
    		sb.append("\n<parameters>");
    		sb.append(serializeQueryParamList(model.getParameters(), true));
        	sb.append("\n</parameters>")
        		.append(setElemAttribute(0, PARSEDSQL, model.getParsedquery()));
		} else {
			sb.append(setElemAttribute(0, ERROR, model.getError()));
		}
    	sb.append("\n</sqlparameters>");
		return sb.toString();
	}

	public String serialize(TdRulesBody sqb) {
		StringBuilder sb=new StringBuilder();
		sb.append("<body>");
		sb.append(setElemAttribute(0, "sql", sqb.getQuery()));
		if (sqb.getSchema()!=null)
			sb.append("\n").append(new TdSchemaXmlSerializer().serialize(sqb.getSchema()));
		sb.append(setElemAttribute(0, "options", sqb.getOptions()));
		sb.append("\n</body>");
		return sb.toString();
	}

}
