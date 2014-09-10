package com.duowan.common.rpc.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.duowan.common.rpc.SerDe;
import com.duowan.common.rpc.SerializeException;

/**
 * 用于支持jsonp的json序列化实现
 * 
 * @author badqiu
 * 
 */
public class JsonpSerDeImpl implements SerDe {
	static String JSONCALLBACK_KEY = "__jsoncallback";

	ObjectMapper objectMapper = new ObjectMapper();
	
	public void serialize(Object object, OutputStream output,Map<String,Object> serializeParams) throws SerializeException {
		if(serializeParams == null) {
			throw new IllegalArgumentException("serializeParams must be not null");
		}
		String callback = (String)serializeParams.get(JSONCALLBACK_KEY);
		if(StringUtils.isBlank(callback)) {
			throw new IllegalArgumentException("not found jsonp callback function name:"+JSONCALLBACK_KEY);
		}
		
		try {
			
			String prefix = callback+"(";
			output.write(prefix.getBytes());
			output.write(objectMapper.writeValueAsBytes(object));
			output.write(")".getBytes());
		}catch(IOException e) {
			throw new SerializeException();
		}
	}

	public Object deserialize(InputStream input, Type returnType,Map<String,Object> params) throws SerializeException {
		throw new UnsupportedOperationException();
	}

}
