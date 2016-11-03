package net.jahhan.dblogistics.doc;

import static java.util.Arrays.asList;
import static org.bson.assertions.Assertions.notNull;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.bson.BsonBinarySubType;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Transformer;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonTypeCodecMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;

import net.jahhan.constant.SystemErrorCode;
import net.jahhan.dblogistics.annotation.DocTransient;
import net.jahhan.dblogistics.entity.SuperEntity;
import net.jahhan.exception.FrameworkException;

public class DocCodec<T extends SuperEntity> implements Codec<T> {
	private static final CodecRegistry DEFAULT_REGISTRY = fromProviders(
			asList(new ValueCodecProvider(), new BsonValueCodecProvider(), new DocumentCodecProvider()));
	private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();
	private static final String ID_FIELD_NAME = "_id";

	private final Class<T> clazz;
	private final Field[] declaredFields;
	private final Map<String, Field> fieldMap;

	private final CodecRegistry registry;
	private final BsonTypeCodecMap bsonTypeCodecMap;
	private final Transformer valueTransformer;

	public DocCodec(Class<T> clazz) {
		this.clazz = clazz;
		this.registry = notNull("registry", DEFAULT_REGISTRY);
		this.bsonTypeCodecMap = new BsonTypeCodecMap(notNull("bsonTypeClassMap", DEFAULT_BSON_TYPE_CLASS_MAP),
				registry);
		this.valueTransformer = new Transformer() {
			@Override
			public Object transform(final Object value) {
				return value;
			}
		};
		this.declaredFields = ArrayUtils.addAll(clazz.getDeclaredFields(), clazz.getSuperclass().getDeclaredFields());
		this.fieldMap = new HashMap<>();
		for (Field field : declaredFields) {
			fieldMap.put(field.getName(), field);
		}
	}

	@Override
	public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
		writer.writeStartDocument();
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			Relationship relationship = field.getAnnotation(Relationship.class);
			StartNode startNode = field.getAnnotation(StartNode.class);
			EndNode endNode = field.getAnnotation(EndNode.class);
			GraphId graphId = field.getAnnotation(GraphId.class);
			DocTransient docTransient = field.getAnnotation(DocTransient.class);
			Object object = null;
			try {
				field.setAccessible(true);
				object = field.get(value);
			} catch (Exception e) {
				FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "文档数据库编码错误！！", e);
			}
			if (null == relationship && null == startNode && null == endNode && null == graphId && null == docTransient
					&& null != object) {
				writer.writeName(field.getName());
				writeValue(writer, encoderContext, object);
			}
		}
		writer.writeEndDocument();
	}

	@Override
	public Class<T> getEncoderClass() {
		return clazz;
	}

	@Override
	public T decode(BsonReader reader, DecoderContext decoderContext) {
		T t = null;
		try {
			t = clazz.newInstance();
		} catch (Exception e) {
		}
		reader.readStartDocument();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			String fieldName = reader.readName();
			Field field = fieldMap.get(fieldName);
			if (null == field) {
				readValue(reader, decoderContext);
				continue;
			}
			field.setAccessible(true);
			try {
				field.set(t, readValue(reader, decoderContext));
			} catch (Exception e) {
				FrameworkException.throwException(SystemErrorCode.DATABASE_ERROR, "文档数据库解码错误！！", e);
			}
		}

		reader.readEndDocument();
		return t;
	}

	private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
		BsonType bsonType = reader.getCurrentBsonType();
		if (bsonType == BsonType.NULL) {
			reader.readNull();
			return null;
		} else if (bsonType == BsonType.ARRAY) {
			return readList(reader, decoderContext);
		} else if (bsonType == BsonType.BINARY) {
			byte bsonSubType = reader.peekBinarySubType();
			if (bsonSubType == BsonBinarySubType.UUID_STANDARD.getValue()
					|| bsonSubType == BsonBinarySubType.UUID_LEGACY.getValue()) {
				return registry.get(UUID.class).decode(reader, decoderContext);
			}
		}
		return valueTransformer.transform(bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
	}

	private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext) {
		reader.readStartArray();
		List<Object> list = new ArrayList<Object>();
		while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
			list.add(readValue(reader, decoderContext));
		}
		reader.readEndArray();
		return list;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
		if (value == null) {
			writer.writeNull();
		} else if (value instanceof Iterable) {
			writeIterable(writer, (Iterable<Object>) value, encoderContext.getChildContext());
		} else if (value instanceof Map) {
			writeMap(writer, (Map<String, Object>) value, encoderContext.getChildContext());
		} else {
			Codec codec = registry.get(value.getClass());
			encoderContext.encodeWithChildContext(codec, writer, value);
		}
	}

	private void writeIterable(final BsonWriter writer, final Iterable<Object> list,
			final EncoderContext encoderContext) {
		writer.writeStartArray();
		for (final Object value : list) {
			writeValue(writer, encoderContext, value);
		}
		writer.writeEndArray();
	}

	private void writeMap(final BsonWriter writer, final Map<String, Object> map, final EncoderContext encoderContext) {
		writer.writeStartDocument();

		beforeFields(writer, encoderContext, map);

		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			writer.writeName(entry.getKey());
			writeValue(writer, encoderContext, entry.getValue());
		}
		writer.writeEndDocument();
	}

	private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext,
			final Map<String, Object> document) {
		if (encoderContext.isEncodingCollectibleDocument() && document.containsKey(ID_FIELD_NAME)) {
			bsonWriter.writeName(ID_FIELD_NAME);
			writeValue(bsonWriter, encoderContext, document.get(ID_FIELD_NAME));
		}
	}

}
