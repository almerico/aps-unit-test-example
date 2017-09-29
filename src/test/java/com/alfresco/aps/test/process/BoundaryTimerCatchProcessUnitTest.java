package com.alfresco.aps.test.process;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

import org.activiti.engine.repository.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alfresco.aps.testutils.AbstractTest;
import com.alfresco.aps.testutils.resources.ActivitiResources;

import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:activiti.cfg.xml", "classpath:common-beans-and-mocks.xml" })
@TestPropertySource(value="classpath:local-dev-test.properties")
public class BoundaryTimerCatchProcessUnitTest extends AbstractTest {
	
	String appName = "Test App";
	String processDefinitionKey = "BoundaryTimerCatchProcess";
	
	@Before
	public void before() throws Exception {
		
		if(env.getProperty ("aps.app.download", Boolean.class, false)){
			ActivitiResources.forceGet(appName);
		}

		Iterator<File> it = FileUtils.iterateFiles(new File(BPMN_RESOURCE_PATH), null, false);
		while (it.hasNext()) {
			String bpmnXml = ((File) it.next()).getPath();
			String extension = FilenameUtils.getExtension(bpmnXml);
			if (extension.equals("xml")) {
				activitiRule.getRepositoryService().createDeployment()
						.addInputStream(bpmnXml, new FileInputStream(bpmnXml)).deploy();
			}
		}
	}

	@After
	public void after() {
		List<Deployment> deploymentList = activitiRule.getRepositoryService().createDeploymentQuery().list();
		for (Deployment deployment : deploymentList) {
			activitiRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
		}
	}

	@Test
	public void testProcessExecution() throws Exception {

		ProcessInstance processInstance = activitiRule.getRuntimeService()
				.startProcessInstanceByKey(processDefinitionKey);

		assertNotNull(processInstance);


		//Assert in seconds and execute/action timer
		unitTestHelpers.assertTimerJobsTimeInSecondsLowerThan(300, true);
		//Assert days and execute/action timer
		unitTestHelpers.assertTimerJobDateLowerThan(1, true);

		unitTestHelpers.assertNullProcessInstance(processInstance.getProcessInstanceId());
	}

}