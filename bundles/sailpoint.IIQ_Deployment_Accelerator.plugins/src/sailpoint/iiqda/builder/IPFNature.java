package sailpoint.iiqda.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import sailpoint.iiqda.IPFPlugin;

public class IPFNature implements IProjectNature {

	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID = IPFPlugin.PLUGIN_ID+".ipfNature";

	private IProject project;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {

//		IProjectDescription desc = project.getDescription();
//		ICommand[] commands = desc.getBuildSpec();
//
//		boolean foundrulebuilder=false;
//		
//		for (int i = 0; i < commands.length; ++i) {
//			if (commands[i].getBuilderName().equals(IIQArtifactBuilder.BUILDER_ID)) {
//				foundrulebuilder=true;
//			}
//		}
//
//		if (foundrulebuilder) return; // nothing to configured
//		
//		ICommand[] newCommands=commands;
//		if(!foundrulebuilder) {
//			ICommand command = desc.newCommand();
//			command.setBuilderName(IIQArtifactBuilder.BUILDER_ID);
//			newCommands=addCommand(newCommands, command);
//		}
//		desc.setBuildSpec(newCommands);
//		project.setDescription(desc, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
//		IProjectDescription description = getProject().getDescription();
//		ICommand[] commands = description.getBuildSpec();
//		for (int i = 0; i < commands.length; ++i) {
//			if (commands[i].getBuilderName().equals(IIQArtifactBuilder.BUILDER_ID)) {
//				ICommand[] newCommands = new ICommand[commands.length - 1];
//				System.arraycopy(commands, 0, newCommands, 0, i);
//				System.arraycopy(commands, i + 1, newCommands, i,
//						commands.length - i - 1);
//				description.setBuildSpec(newCommands);
//				project.setDescription(description, null);			
//				return;
//			}
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

//	private ICommand[] addCommand(ICommand[] comms, ICommand command) {
//		
//		if(command==null) return comms;
//		if(comms==null) comms=new ICommand[0];
//		
//		ICommand[] ret=new ICommand[comms.length+1];
//		int i=0;
//		for(i=0;i<comms.length;i++) {
//			ret[i]=comms[i];
//		}
//		ret[i]=command;
//		return ret;
//		
//		
//	}
	
}
