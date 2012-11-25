import static org.junit.Assert.*;
import static org.sweble.wikitext.articlecruncher.JobProcessingState.*;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Before;
import org.junit.Test;
import org.sweble.wikitext.articlecruncher.Job;
import org.sweble.wikitext.articlecruncher.JobGeneratorFactory;
import org.sweble.wikitext.articlecruncher.JobTrace;
import org.sweble.wikitext.articlecruncher.JobTraceSet;
import org.sweble.wikitext.articlecruncher.JobWithHistory;
import org.sweble.wikitext.articlecruncher.Nexus;
import org.sweble.wikitext.articlecruncher.ProcessedJob;
import org.sweble.wikitext.articlecruncher.ProcessingNodeFactory;
import org.sweble.wikitext.articlecruncher.Processor;
import org.sweble.wikitext.articlecruncher.Result;
import org.sweble.wikitext.articlecruncher.StorerFactory;
import org.sweble.wikitext.articlecruncher.pnodes.LocalProcessingNode;
import org.sweble.wikitext.articlecruncher.pnodes.LpnJobProcessorFactory;
import org.sweble.wikitext.articlecruncher.utils.AbortHandler;
import org.sweble.wikitext.articlecruncher.utils.WorkerBase;

import de.fau.cs.osr.utils.WrappedException;

public class LpnCruncherTest
{
	private Nexus nexus;
	
	private static final long NUM_JOBS_TO_GENERATE = (long) Math.pow(2, 22);
	
	private static final int NUM_WORKERS = 256;
	
	private AtomicLong generated = new AtomicLong(0);
	
	private AtomicLong processed = new AtomicLong(0);
	
	private AtomicLong stored = new AtomicLong(0);
	
	//private long failAfter = -1;
	
	@Before
	public void before() throws Throwable
	{
		nexus = Nexus.get();
		
		nexus.setUp(
				16, /* in tray capacity */
				16, /* processed jobs capacity */
				16, /* out tray capacity */
				false /* no console output */);
		
		JobGeneratorFactory jobFactory = createJobFactory();
		nexus.addJobGenerator(jobFactory);
		
		ProcessingNodeFactory pnFactory = createPnFactory();
		nexus.addProcessingNode(pnFactory);
		
		StorerFactory storerFactory = createStorerFactory();
		nexus.addStorer(storerFactory);
	}
	
	/*
	@Test
	public void testWithFailing() throws Throwable
	{
		failAfter = (numJobsToGenerate * 3) / 4;
		
		try
		{
			nexus.start();
		}
		catch (RuntimeException e)
		{
			assertEquals("Die!", e.getMessage());
		}
		
		Set<JobTrace> jobTraces = Nexus.get().getJobTraces();
		System.out.println("numJobsToGenerate: " + numJobsToGenerate);
		System.out.println("generated: " + generated.get());
		System.out.println("processed: " + processed.get());
		System.out.println("stored: " + stored.get());
		System.out.println("job traces left: " + jobTraces.size());
	}
	*/
	
	@Test
	public void testWithoutFailing() throws Throwable
	{
		nexus.start();
		
		assertEquals(NUM_JOBS_TO_GENERATE, generated.get());
		
		assertEquals(NUM_JOBS_TO_GENERATE, processed.get());
		
		assertEquals(NUM_JOBS_TO_GENERATE, stored.get());
		
		Set<JobTrace> jobTraces = Nexus.get().getJobTraces();
		assertTrue(jobTraces.isEmpty());
	}
	
	// =========================================================================
	
	private JobGeneratorFactory createJobFactory()
	{
		return new JobGeneratorFactory()
		{
			@Override
			public WorkerBase create(
					final AbortHandler abortHandler,
					final BlockingQueue<JobWithHistory> inTray,
					final JobTraceSet jobTraces)
			{
				try
				{
					return new WorkerBase("JobGenerator", abortHandler)
					{
						@Override
						protected void work() throws InterruptedException
						{
							for (long i = 0; i < NUM_JOBS_TO_GENERATE; ++i)
							{
								// generate jobs out of thin air.
								Job job = new TestJob();
								generated.incrementAndGet();
								
								JobTrace trace = job.getTrace();
								trace.signOff(getClass(), null);
								
								jobTraces.add(trace);
								
								inTray.put(new JobWithHistory(job));
							}
						}
					};
				}
				catch (Exception e)
				{
					throw new WrappedException(e);
				}
			}
		};
	}
	
	private ProcessingNodeFactory createPnFactory()
	{
		return new ProcessingNodeFactory()
		{
			@Override
			public WorkerBase create(
					AbortHandler abortHandler,
					BlockingQueue<JobWithHistory> inTray,
					BlockingQueue<JobWithHistory> processedJobs)
			{
				return new LocalProcessingNode(
						abortHandler,
						inTray,
						processedJobs,
						createLpnFactory(),
						NUM_WORKERS);
			}
			/*
			@Override
			public WorkerBase create(
					final AbortHandler abortHandler,
					final BlockingQueue<JobWithHistory> inTray,
					final BlockingQueue<JobWithHistory> processedJobs)
			{
				return new WorkerBase("ProcessingNode", abortHandler)
				{
					@Override
					protected void work() throws InterruptedException
					{
						while (true)
						{
							JobWithHistory jobWithHistory = inTray.take();
							Job job = jobWithHistory.getJob();
							
							job.getTrace().signOff(getClass(), null);
							
							// do process.
							if (failAfter >= 0 && generated.get() > failAfter)
								throw new RuntimeException("Die!");
							
							processed.incrementAndGet();
							
							Result result = new Result(job, (Object) null);
							
							ProcessedJob processed = new ProcessedJob(
									FAILED,
									job,
									result);
							
							processedJobs.put(new JobWithHistory(jobWithHistory, processed));
						}
					}
				};
			}
			*/
		};
	}
	
	private LpnJobProcessorFactory createLpnFactory()
	{
		return new LpnJobProcessorFactory()
		{
			@Override
			public Processor createProcessor()
			{
				return new Processor()
				{
					@Override
					public ProcessedJob process(JobWithHistory jobWithHistory)
					{
						Job job = jobWithHistory.getJob();
						
						job.getTrace().signOff(getClass(), null);
						
						Result result = new Result(job, (Object) null);
						
						ProcessedJob processed = new ProcessedJob(
								FAILED,
								job,
								result);
						
						return processed;
					}
				};
			}
		};
	}
	
	private StorerFactory createStorerFactory()
	{
		return new StorerFactory()
		{
			@Override
			public WorkerBase create(
					final AbortHandler abortHandler,
					final JobTraceSet jobTraces,
					final BlockingQueue<ProcessedJob> outTray)
			{
				return new WorkerBase("Storer", abortHandler)
				{
					@Override
					protected void work() throws InterruptedException
					{
						while (true)
						{
							ProcessedJob processedJob = outTray.take();
							
							TestJob job = (TestJob) processedJob.getJob();
							
							JobTrace trace = job.getTrace();
							trace.signOff(getClass(), null);
							
							// do store.
							stored.incrementAndGet();
							
							if (!jobTraces.remove(trace))
								throw new InternalError("Missing job trace");
						}
					}
				};
			}
		};
	}
	
	private static final class TestJob
			extends
				Job
	{
		private final JobTrace trace = new JobTrace();
		
		@Override
		public JobTrace getTrace()
		{
			return trace;
		}
	}
}
