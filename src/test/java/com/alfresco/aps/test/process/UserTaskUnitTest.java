package com.alfresco.aps.test.process;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.assertions.ProcessInstanceAssert;
import com.alfresco.aps.testutils.assertions.TaskAssert;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static com.alfresco.aps.testutils.TestUtilsConstants.*;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
public class UserTaskUnitTest extends AbstractBpmnTest {

	/*
	 * Setting the App name to be downloaded if run with -Daps.app.download=true
	 * Also set the process definition key of the process that is being tested
	 */
	static {
		appName = "Test App";
		processDefinitionKey = "UserTaskProcess";
	}

	@Test
	public void testProcessExecution() throws Exception {
		/*
		 * Creating a map and setting a variable called "initiator" when
		 * starting the process.
		 */
		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");

		/*
		 * Starting the process using processDefinitionKey and process variables
		 */
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey, processVars);

		/*
		 * Once started assert that the process instance is not null and
		 * successfully started
		 */
		assertNotNull(processInstance);

		/*
		 * Since the next step after start is a user task, doing a query to find
		 * the user task count in the engine. Assert that it is only 1
		 */
		assertEquals(1, taskService.createTaskQuery().count());

		/*
		 * Get the Task object for further task assertions
		 */
		Task task = taskService.createTaskQuery().singleResult();

		/*
		 * Asserting the task for things such as assignee, due date etc. Also,
		 * at the end of it complete the task Using the custom assertion
		 * TaskAssert from the utils project here
		 */
		TaskAssert.assertThat(task).hasAssignee("$INITIATOR", false, false).hasDueDate(2, TIME_UNIT_DAY).complete();

		/* 
		 * Using the custom assertion ProcessInstanceAssert, make sure that the
		 * process is now ended.
		 */
		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}