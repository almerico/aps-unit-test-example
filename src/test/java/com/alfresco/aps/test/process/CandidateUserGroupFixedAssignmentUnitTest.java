package com.alfresco.aps.test.process;

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
public class CandidateUserGroupFixedAssignmentUnitTest extends AbstractBpmnTest {

	static {
		appName = "Test App";
		processDefinitionKey = "CandidateUserGroupFixedAssignment";
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);

		assertEquals(1, taskService.createTaskQuery().count());
		Task task = taskService.createTaskQuery().singleResult();

		TaskAssert.assertThat(task).hasAssignee("assignee", false, null)
				.hasCandidateGroups(new String[] { "group1", "group2", "group3" }, false, null)
				.hasCandidateUsers(new String[] { "user1", "user2", "user3" }, false, null).complete();

		ProcessInstanceAssert.assertThat(processInstance).isComplete();
	}

}