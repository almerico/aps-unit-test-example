package com.alfresco.aps.test.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractBpmnTest;
import com.alfresco.aps.testutils.ProcessInstanceAssert;
import com.alfresco.aps.testutils.TaskAssert;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class CandidateUserAssignmentUnitTest extends AbstractBpmnTest {
	
	static {
		appName = "Test App";
		processDefinitionKey = "CandidateUserAssignment";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);
		
		assertEquals(1, taskService.createTaskQuery().count());
		
		Task task = taskService.createTaskQuery().singleResult();
		
		TaskAssert.assertThat(task).hasCandidateUsers(new String[]{"user1@example.com", "user2@example.com"}, true, false).complete();
		
		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}