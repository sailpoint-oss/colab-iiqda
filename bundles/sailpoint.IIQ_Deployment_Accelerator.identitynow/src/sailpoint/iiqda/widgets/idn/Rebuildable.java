package sailpoint.iiqda.widgets.idn;

public interface Rebuildable {

  // Empty interface to flag objects that require a rebuild when they change
  // This indicates to the Editor that if a Rebuildable object has changed,
  // The UI needs to be rebuilt, i.e. when a Transform gets a new parent/child
  // or a list of Transforms changes content
  
}
