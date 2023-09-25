package sailpoint.iiqda.objects.idn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import sailpoint.iiqda.wizards.importresource.idn.IDNObject;

public class Transform implements IDNObject {

  private String id;
  private Type type;
  private Transform input;
  @SerializedName("attributes") private Map<String,Object> attributeValues;
  private transient String unknownType;
  
  public enum AttributeType {
    STRING (""),
    LIST ("L"),
    TABLE ("T"),
    BOOLEAN ("B"),
    INTEGER ("I");
    
    private String identifier;
    
    AttributeType(String identifier) {
      this.identifier=identifier;
    }
    
    public String getIdentifier() {
      return identifier;
    }
    
    public static AttributeType getTypeOf(String identifier) {
      for (AttributeType value: AttributeType.values()) {
        if (value.getIdentifier().equals(identifier)) {
          return value;
        }
      }
      return AttributeType.STRING;
    }

  }
  
  public static class Attribute {
    
    private AttributeType type;
    private String name;
    private boolean required;
    private Object value;
    
    public Attribute(String attrDesc) {
      if (attrDesc.startsWith("[")) {
        type=AttributeType.getTypeOf(attrDesc.substring(1,2));
        attrDesc=attrDesc.substring(2);      
      } else {
        type=AttributeType.STRING;
      }
      if (attrDesc.startsWith("*")) {
        required=true;
        attrDesc=attrDesc.substring(1);        
      } else {
        required=false;
      }
      name=attrDesc;
    }
    
    public String getName() {
      return name;
    }
    public AttributeType getType() {
      return type;
    }
    public boolean isRequired() {
      return required;
    }
  }
  
  public enum Type {
    
    ACCOUNTATTRIBUTE ("Account Attribute", "accountFilter|applicationId|applicationName|*attributeName|*sourceName", "accountAttribute"),
    CONCAT           ("Concatenate Values", "[Lvalues"),
    //DATEFORMAT
    DECOMPOSEDIACRITICALMARKS ("Decompose Diacritical Marks", "decomposeDiacriticalMarks"),
    E164PHONE        ("E.164 Format Phone Number", null),
    FIRSTVALID       ("First valid value from list", "[L*values|[BignoreErrors", "firstValid"),
    INDEXOF          ("Find first location of substring", "*substring", "indexOf"),
    //ISO3166
    LASTINDEXOF      ("Find last location of substring", "*substring", "lastIndexOf"),
    LOOKUP           ("Lookup Value", "[Ttable"),
    LOWER            ("Convert to lower case", null),    
    REFERENCE        ("Reference External Transform", "*id"),
    REPLACE          ("Regular Expression Replacement", "*regex|*replacement"),
    REPLACEALL       ("Multiple Regular Expression Replacement", "[Ttable", "replaceAll"),
    STATIC           ("Render Value", "*value"),
    SUBSTRING        ("Substring", "[I*begin|[IbeginOffset|[I*end|[IendOffset"),
    TRIM             ("Trim whitespace", null),
    UPPER            ("Convert to upper case", null),
    UNKNOWN          ("Unknown Attribute Type", null);
    
    private String description;
    private List<Attribute> attributes;
    private String nonLowerName=null;
    
    Type (String description, String attrs) {
      this.description=description;
      this.attributes=new ArrayList<Attribute>();
      if (attrs!=null) {
        String[] attrDescriptions=attrs.split("\\|");
        for (String desc: attrDescriptions) {
          this.attributes.add(new Transform.Attribute(desc));
        }
      }
    }
    
    Type (String description, String attrs, String nonLower) {
      this(description, attrs);
      this.nonLowerName=nonLower;
    }
    
    public List<Attribute> getAttributes() {
      return attributes;
    }
    
    public String getDescription() {
      return description;
    }
    
    public String getType() {
      if (nonLowerName!=null) return nonLowerName;
      return this.toString().toLowerCase();
    }
    
    public String getNonLower() {
      return nonLowerName;
    }
    
    public static Type getTypeOf(String identifier) {
      for (Type value: Type.values()) {
        if (value.getType().equals(identifier)) {
          return value;
        }
        if (value.getNonLower()!=null) {
          if (value.getNonLower().equals(identifier)) {
            return value;
          }
        }
      }
      return Type.UNKNOWN;
    }
    
    public static Type[] valuesNoUnknown() {
      Type[] values=values();
      Type[] noUnknown=new Type[values.length-1];
      int i=0;
      for (Type val: values) {
        if (val!=Type.UNKNOWN) {
          noUnknown[i++]=val;
        }
      }
      return noUnknown;
    }

    public boolean hasAttributes() {      
      return getAttributes().size()>0;
    }
    
  }

  public Transform() {
    attributeValues=new HashMap<String,Object>();
    input=null;
  }
  
  public Transform(Transform old) {
    this();
    id = old.getId();
    type = old.getTransformType();
    input = old.getInput();
    if (input!=null) {
      input=new Transform(input); // if not null, make a copy
    }
    // This doesn't work for complex objects like a List of Transforms, for example
    // attributeValues = new HashMap<String,Object>(old.getAttributes());
    // So let's do some special case-fu
    attributeValues=copyMap(old.getAttributes());
  }

  public Transform(Map map) {
    this();
    setId((String)map.get("id"));
    setType((String)map.get("type"));
    setAttributes((Map)map.get("attributes"));
  }
  
  public Transform(Type type) {
    this();
    id=null;
    this.type=type;
  }
  

  public String getId() {
//    System.out.println("Transform.getId: ("+id+")");

    return id;
  }

  public void setId(String id) {
//    System.out.println("Transform.setId: ("+id+")");

    this.id = id;
  }

  public String getType() {
//    System.out.println("Transform.getType: ("+type+")");
    if (type==null) return null;
    return type.getType();
  }

  public void setType(String type) {
    System.out.println("Transform.setType: ("+type+")");
    this.type = Type.getTypeOf(type);
  }

  public Type getTransformType() {
//    System.out.println("Transform.getTransformType: "+type);

    return type;
  }
  
  public Map<String, Object> getAttributes() {
//    System.out.println("Transform.getAttributes: ");

    return attributeValues;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
	public void setAttributes(Map attrValues) {
//    System.out.println("Transform.setAttributes: "+attributeValues);

    this.attributeValues = attrValues;
    if (attributeValues!=null) {
      // convert maps inside Lists to Transforms - why, the unmarshaler doesn't do this: I don't know.
      // TODO: fix unmarshaler
      for (Object key: attrValues.keySet()) {
        Object value=attrValues.get(key);
        if (value instanceof List) {
          List outValue=new ArrayList();
          for (Object lValue: ((List)value)) {
            if (lValue instanceof Map && ((Map)lValue).get("type")!=null) {
              outValue.add( new Transform((Map)lValue) );              
            } else {
              outValue.add(lValue);
            }
          }
          attrValues.put(key, outValue);
        }
      }
      
    }
    if (attributeValues!=null && attributeValues.get("input")!=null) {
      input=new Transform((Map)this.attributeValues.get("input"));
      this.attributeValues.remove("input");
    }
    // TODO: Think about subclassing Transform for transform-specific stuff (such as here, where
    // 'values' on a firstValid is a list of Transforms, not HashMaps
/*    if (type==Transform.Type.FIRSTVALID) { 
      List<Map> maps=(List<Map>)attributeValues.get("values");
      if (maps!=null) {
        List xforms=new ArrayList();
        for (Map map: maps) {
          xforms.add(new Transform(map));
        }
        attributeValues.put("values",  xforms);
      }
    }
 */
    
  }
  
  public String getUnknownType() {
    return unknownType;
  }
  public Transform getInput() {
//    System.out.println("Transform.getInput: ");

    return input;
  }
  public void setInput(Transform child) {
//    System.out.println("Transform.setInput: "+child);

    this.input=child;
  }
  public boolean hasInput() {
    return input!=null;
  }
  
  public void setTransformType(Type type) {
    this.type=type;
  }

  public Object getAttribute(String name) {
    if (attributeValues!=null) {
      return attributeValues.get(name);
    }
    return null;
  }

  public void setAttribute(String name, Object value) {
    attributeValues.put(name,  value);
  }
  
  public boolean equals(Object other) {
    
    if (this==other) return true;
    if (!(other instanceof Transform)) return false;
    Transform tOther=(Transform)other;    
    return isEqual(id, tOther.getId()) &&
           isEqual(type, tOther.getTransformType()) &&
           isEqual(attributeValues, tOther.getAttributes()) &&
           isEqual(input, tOther.getInput());
        
    
  }
  
  private boolean isEqual(Object a, Object b) {
    // special String thing: empty string and null are the same
    if (a instanceof String && ((String) a).length()==0) a=null;
    if (b instanceof String && ((String) b).length()==0) b=null;
    if (a==null && b!=null) return false;
    if (a!=null && b==null) return false;
    if (a==null && b==null) return true;
    return a.equals(b);
  }

  private List copyList(List original) {
    List newList=new ArrayList();
    for (Object obj: original) {
      if (obj instanceof String) {
        newList.add(new String((String)obj));
      } else if (obj instanceof Transform) {
        newList.add(new Transform((Transform)obj));
      } else {
        throw new IllegalArgumentException("CopyList: unable to copy object type "+obj.getClass().getName());
      }
    }
    return newList;
  }
    
  private Map<String, Object> copyMap(Map<String, Object> oldMap) {
    if (oldMap==null) return null;
    Map<String,Object> newMap = new HashMap<String,Object>();
    for (String key: oldMap.keySet()) {
      Object value=oldMap.get(key);
      if (value instanceof List) {
        value=copyList((List)value);
      } else if (value instanceof String) {
        value=new String((String)value); 
      } else if (value instanceof Map) {
        value=copyMap((Map<String, Object>) value);
      }
      else throw new IllegalArgumentException("CopyList: unable to copy object type "+value.getClass().getName());
      newMap.put(key, value);
    }
    return newMap;
  }

  public void removeAttribute(String name) {
    attributeValues.remove(name);
  }
  
  public String toString() {
    StringBuilder sb=new StringBuilder();
    sb.append("id: "+id+"\n");
    sb.append("type: "+type+"\n");
    if (attributeValues!=null) {
      sb.append("attributes:\n");
      for (String key: attributeValues.keySet()) {
        sb.append("  key:   "+key+"\n");
        Object value=attributeValues.get(key);
        if (value instanceof List) {
          sb.append("  list:\n");
          for (Object val: (List)value) {
            if (val instanceof Transform) {
              sb.append(indent(2, ((Transform)val).toString()));
            } else {
              sb.append(indent(2, (String)val));
            }
            sb.append("\n");
          }
        } else {
          sb.append("  value: "+value+"\n");
        }
      }
    }
    if (input!=null) {
      sb.append("input: \n");
      sb.append(indent(2, input.toString()));
    }
    return sb.toString();
  }
  
  private String indent(int indent, String value) {
    String output=value.replaceAll("\n", "\n  ");
    return "  "+output;
  }

  public void setInputTransform(Transform newXform) {
    this.input=newXform;
    
  }

	@Override
	public String getName() {
		return id;
	}
  
}

