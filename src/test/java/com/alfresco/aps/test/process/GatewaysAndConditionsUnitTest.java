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
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
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

		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		processVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Approve");

		TaskAssert.assertThat(task).hasAssignee("$INITIATOR", false, false).complete(taskCompleteVars);

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
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

		Map<String, Object> taskCompleteVars = new HashMap<String, Object>();
		taskCompleteVars.put(unitTestHelpers.getTaskOutcomeVariable(task), "Reject");

		TaskAssert.assertThat(task).hasAssignee("$INITIATOR", false, false).complete(taskCompleteVars);

		assertEquals(1, taskService.createTaskQuery().count());
		Task rejectTask = taskService.createTaskQuery().singleResult();

		TaskAssert.assertThat(rejectTask).hasName("rejected").hasAssignee("$INITIATOR", false, false)
				.complete();

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}
}