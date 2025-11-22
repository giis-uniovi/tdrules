package giis.tdrules.model.io;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import giis.tdrules.model.shared.ModelException;

public class ModelJsonSerializer {

	/**
	 * Recommended serialization for models (exclude null and empty attributes)
	 */
	public String serialize(Object model, boolean prettyPrint) {
		ObjectMapper mapper = new ObjectMapper()
				.setDefaultPropertyInclusion(Include.NON_NULL)
				.setDefaultPropertyInclusion(Include.NON_EMPTY);
		try {
			return prettyPrint 
					? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model)
					: mapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			throw new ModelException("Exception serializing json", e);
		}
	}

	/**
	 * Recommended deserialization for models
	 */
	public Object deserialize(String json, Class<?> clazz) {
		// habitualmente uso FAIL_ON_UNKNOWN_PROPERTIES a falso, pero aqui debemos forzar
		// a que el objeto este bien formado, por lo que uso los valores por defecto
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			throw new ModelException("Exception deserializing object", e);
		}
	}

}
