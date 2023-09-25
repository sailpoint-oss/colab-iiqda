package sailpoint.iiqda.widgets.idn;

import org.eclipse.swt.events.TypedEvent;

public class TransformChangeEvent extends TypedEvent {

  /**
	 * 
	 */
	private static final long serialVersionUID = -4602448756732600187L;

	public enum Type {
  		DELETE_TRANSFORM,
  		TYPE_CHANGE,
  		NEW_PARENT,
  		NEW_CHILD, 
  		WIDGET_CHANGE, 
  		DATA_CHANGE, EVENT_HANDLED, REORDER_TRANSFORMS, REMOVED, INSERT, REBUILD, ADD, WRAP_INPUT
  };

  private Type type;
  private Object object;
  
  public TransformChangeEvent(Type type) {
  	this(type, null);
  }
  
	public TransformChangeEvent(Type type, Object object) {
    super(object);
    this.type=type;
    this.object=object;
  }
	
	public Type getType() {
		return type;
	}
	
	public Object getSource() {
		return object;
	}

}
