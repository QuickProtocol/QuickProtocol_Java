package Quick.Protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

/**
 * 命令信息
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class QpCommandInfo {
	private static ObjectMapper mapper = new ObjectMapper();
	private static JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

	/**
	 * 名称
	 */
	public String Name;
	/**
	 * 描述
	 */
	public String Description;
	/**
	 * 请求类型
	 */
	public String RequestTypeName;
	/**
	 * 请求定义
	 */
	public String RequestTypeSchema;
	/**
	 * 请求示例
	 */
	public String RequestTypeSchemaSample;
	/**
	 * 响应类型
	 */

	public String ResponseTypeName;
	/**
	 * 响应定义
	 */
	public String ResponseTypeSchema;
	/**
	 * 响应示例
	 */
	public String ResponseTypeSchemaSample;

	private Class requestType;
	private Class responseType;

	public QpCommandInfo() {
	}

	public QpCommandInfo(String name, String description, Class requestType, Class responseType) {
		Name = name;
		Description = description;

		try {
			this.requestType = requestType;
			RequestTypeName = requestType.getName();
			JsonSchema requestTypeSchema = schemaGen.generateSchema(requestType);
			RequestTypeSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestTypeSchema);
			RequestTypeSchemaSample = mapper.writeValueAsString(requestType.getDeclaredConstructor().newInstance());

			this.responseType = responseType;
			ResponseTypeName = responseType.getName();

			JsonSchema responseTypeSchema = schemaGen.generateSchema(responseType);
			ResponseTypeSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseTypeSchema);
			ResponseTypeSchemaSample = mapper.writeValueAsString(responseType.getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取命令请求类型
	 * 
	 * @return
	 */
	public Class GetRequestType() {
		if (requestType != null)
			return requestType;
		try {
			return Class.forName(RequestTypeName);
		} catch (ClassNotFoundException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	/**
	 * 获取命令响应类型
	 * 
	 * @return
	 */
	public Class GetResponseType() {
		if (responseType != null)
			return responseType;
		try {
			return Class.forName(ResponseTypeName);
		} catch (ClassNotFoundException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	/**
	 * 创建命令信息实例
	 * 
	 * @param <TResponse>
	 * @param request
	 * @param responseType
	 * @return
	 */
	public static <TResponse> QpCommandInfo Create(IQpCommandRequest<TResponse> request,
			Class<TResponse> responseType) {

		Class requestType = request.getClass();

		String name = null;
		if (name == null) {
			DisplayName ann = (DisplayName) requestType.getAnnotation(DisplayName.class);
			if (ann != null)
				name = ann.value();
		}
		if (name == null)
			name = requestType.getCanonicalName();
		if (name == null)
			name = requestType.getName();

		String description = null;
		if (description == null) {
			Description ann = (Description) requestType.getAnnotation(Description.class);
			if (ann != null)
				description = ann.value();
		}
		return new QpCommandInfo(name, description, requestType, responseType);
	}
}
