package com.alfresco.aps.test.process;

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
@TestPropertySource(value="classpath:local-dev-test.properties")
public class CandidateGroupAssignmentUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "CandidateGroupAssignment";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);
		
		assertEquals(1, taskService.createTaskQuery().count());
		Task task = taskService.createTaskQuery().singleResult();
		unitTestHelpers.assertCandidateAssignment(new String[]{"group1", "group2"}, null, task, true, false);
		
		taskService.complete(task.getId());

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}