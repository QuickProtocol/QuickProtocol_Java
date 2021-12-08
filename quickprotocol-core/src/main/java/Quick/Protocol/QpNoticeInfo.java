package Quick.Protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

/**
 * 通知信息
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class QpNoticeInfo {

	private static ObjectMapper mapper = new ObjectMapper();
	private static JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

	private Class noticeType;

	/**
	 * 名称
	 */
	public String Name;
	/**
	 * 描述
	 */
	public String Description;
	/**
	 * 通知类型名称
	 */
	public String NoticeTypeName;

	public QpNoticeInfo() {
	}

	
	public QpNoticeInfo(String name, String description, Class noticeType) {
		Name = name;
		Description = description;
		this.noticeType = noticeType;
		NoticeTypeName = noticeType.getName();

		try {
			JsonSchema schema = schemaGen.generateSchema(noticeType);
			NoticeTypeSchema = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
			NoticeTypeSchemaSample = mapper.writeValueAsString(noticeType.getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/// <summary>
	/// 获取通知类型
	/// </summary>
	public Class GetNoticeType() {
		if (noticeType != null)
			return noticeType;
		try {
			return Class.forName(NoticeTypeName);
		} catch (ClassNotFoundException e) {
			throw new java.lang.RuntimeException(e);
		}
	}

	/**
	 * 定义
	 */
	public String NoticeTypeSchema;
	/**
	 * 示例
	 */
	public String NoticeTypeSchemaSample;

	/**
	 * 创建通知信息实例
	 * 
	 * @param type
	 * @return
	 */
	public static QpNoticeInfo Create(Class type) {
		String name = null;
		if (name == null) {
			DisplayName ann = (DisplayName) type.getAnnotation(DisplayName.class);
			if (ann != null)
				name = ann.value();
		}
		if (name == null)
			name = type.getCanonicalName();
		if (name == null)
			name = type.getName();

		String description = null;
		if (description == null) {
			Description ann = (Description) type.getAnnotation(Description.class);
			if (ann != null)
				description = ann.value();
		}
		return new QpNoticeInfo(name, description, type);
	}

	/**
	 * 创建通知信息实例
	 * 
	 * @param instance
	 * @return
	 */
	public static QpNoticeInfo Create(Object instance) {
		return Create(instance.getClass());
	}
}
