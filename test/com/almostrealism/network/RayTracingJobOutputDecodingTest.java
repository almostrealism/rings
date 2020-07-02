package com.almostrealism.network;

import org.almostrealism.io.JobOutput;
import org.junit.Test;

public class RayTracingJobOutputDecodingTest {
	@Test
	public void encodeWithUserAndPassword() {
		RayTracingJobOutput output =
				new RayTracingJobOutput("task", "user",
								"password", "task:2:3:4:5");

		assert output.getTaskId().equals("task");
		assert output.getUser().equals("user");
		assert output.getPassword().equals("password");
		assert output.getX() == 2;
		assert output.getY() == 3;
		assert output.getDx() == 4;
		assert output.getDy() == 5;
	}

	@Test
	public void encodeWithoutUserAndPassword() {
		RayTracingJobOutput output =
				new RayTracingJobOutput("task", "",
						"", "task:2:3:4:5");

		assert output.getTaskId().equals("task");
		assert output.getUser().equals("");
		assert output.getPassword().equals("");
		assert output.getX() == 2;
		assert output.getY() == 3;
		assert output.getDx() == 4;
		assert output.getDy() == 5;
	}

	@Test
	public void decodeWithoutUserAndPassword() {
		RayTracingJobOutput output = (RayTracingJobOutput) JobOutput.decode("com.almostrealism.network.RayTracingJobOutput:2888305c-3ae3-4ac1-bef8-6d2a3629158a:::67:2888305c-3ae3-4ac1-bef8-6d2a3629158a:60:24:8:8:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]:[0.00, 0.00, 0.00]");
		assert output.getTaskId().equals("2888305c-3ae3-4ac1-bef8-6d2a3629158a");
		assert output.getUser().equals("");
		assert output.getPassword().equals("");
		assert output.getX() == 60;
		assert output.getY() == 24;
		assert output.getDx() == 8;
		assert output.getDy() == 8;
	}
}
