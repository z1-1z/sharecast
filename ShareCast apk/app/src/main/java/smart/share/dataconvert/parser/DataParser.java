package smart.share.dataconvert.parser;

import java.io.InputStream;
import java.util.List;

public interface DataParser {
	public List<?> parse(InputStream is, int type) throws Exception;
	
	public String serialize(List<?> models, int responseStyle) throws Exception;
}
