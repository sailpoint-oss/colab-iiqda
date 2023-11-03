package sailpoint.iiqda.testing.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class UITesting {

	private static final String TEST_PROJECT_NAME = "UI Test";
	private static SWTWorkbenchBot bot;
	private final Log logger = LogFactory.getLog("sailpoint.iiqda.UITesting");

	@BeforeClass
	public static void beforeClass() throws Exception {
		// don't use SWTWorkbenchBot here which relies on Platform 3.x
		bot = new SWTWorkbenchBot();

	}

	@Test
	public void ensurePluginIsAvailable() {
		
		log("EnsurePluginIsAvailable");		
				
		// New Project Dialog
		SWTBotMenu menu=bot.menu("File").menu("New").menu("Project...").click();
		
		// Expand SailPoint
		SWTBotTree tree=bot.tree();
		SWTBotTreeItem treeItem = tree.getTreeItem("SailPoint");
		assert(treeItem!=null);
		treeItem.expand();
		
		// Select IdentityIQ Project
		SWTBotTreeItem node = treeItem.getNode("IdentityIQ Project");
		assert(node!=null);
		node.select();

		// Next
		SWTBotButton button = bot.button("&Next >");
		assert(button!=null);
		button.click();
		
		// Enter Project Name, select to take libraries from existing project
		bot.textWithLabel("&Project name:").setText(TEST_PROJECT_NAME);
		bot.tabItem("Existing Project").activate();
		
		assert(bot.comboBox("Sample")!=null);
		
		SWTBotShell wizard=bot.activeShell();
		// Finish
		bot.button("Finish").click();
		
		// Wait for project creation to finish
		bot.waitUntil(Conditions.shellCloses(wizard));
		
		// Verify the project exists
		SWTBotView view = bot.viewByTitle("Project Explorer");
		assert(view!=null);
		SWTBotTree projTree = view.bot().tree();
		
		SWTBotTreeItem project=null;
		for (SWTBotTreeItem item: projTree.getAllItems()) {
			if (item.getText().equals(TEST_PROJECT_NAME)) {
				project=item;
			}
		}
		assert(project!=null);
		
		// Check the contents;
		project.expand();
		for (SWTBotTreeItem item: project.getItems()) {
			log("Project Item: "+item.getText());
		}
		wait (10);
	}

	private void wait(int seconds) {
		try { 
			Thread.sleep(1000*seconds);
		} catch (Exception e) {
		}
	}
	
	private void log(String msg) {
        //getLog().log(new Status(Status.INFO, "UITesting", Status.OK, msg, null));
		logger.info(msg);
	}
}
