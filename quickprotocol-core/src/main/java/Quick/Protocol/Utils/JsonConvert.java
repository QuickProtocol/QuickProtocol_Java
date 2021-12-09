package Quick.Protocol.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonConvert {
	private static ObjectMapper mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	public static String SerializeObject(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object DeserializeObject(String content, Class<?> type) {
		try {
			return mapper.readValue(content, type);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
