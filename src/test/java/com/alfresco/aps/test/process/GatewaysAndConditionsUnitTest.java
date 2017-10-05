package com.alfresco.aps.test.process;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value = "classpath:local-dev-test.properties")
public class GatewaysAndConditionsUnitTest extends AbstractBpmnTest {

	static {
		appName = "Test App";
		processDefinitionKey = "GatewaysAndConditions";
	}

	@Test
	public void testProcessExecutionApprovePath() throws Exception {

		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey, processVars);

		assertNotNull(processInstance);

		assertEquals(1, taskService.createTaskQuery().count());

		Task task = taskService.createTaskQuery().singleResult();

		unitTestHelpers.assertUserAssignment("$INITIATOR", task, false, false);

		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		processVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Approve");

		taskService.complete(task.getId(), taskCompleteVars);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

	@Test
	public void testProcessExecutionRejectPath() throws Exception {

		Map<String, Object> processVars = new HashMap<String, Object>();
		processVars.put("initiator", "$INITIATOR");
		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey, processVars);

		assertNotNull(processInstance);

		assertEquals(1, taskService.createTaskQuery().count());

		Task task = taskService.createTaskQuery().singleResult();

		unitTestHelpers.assertUserAssignment("$INITIATOR", task, false, false);

		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		taskCompleteVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Reject");
		taskService.complete(task.getId(), taskCompleteVars);

		assertEquals(1, taskService.createTaskQuery().count());
		Task rejectTask = taskService.createTaskQuery().singleResult();
		assertEquals("rejected", rejectTask.getName());
		unitTestHelpers.assertUserAssignment("$INITIATOR", rejectTask, false, false);
		taskService.complete(rejectTask.getId());

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}
}