
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

/**
 * This is a very simple example of the RinSim simulator that shows how a
 * simulation is set up. It is heavily documented to provide a sort of
 * 'walk-through' experience for new users of the simulator.<br>
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 *
 * @author Rinde van Lon
 */
public final class MyTestExample {

	static final double VEHICLE_SPEED_KMH = 50d;
	static final Point MIN_POINT = new Point(0, 0);
	static final Point MAX_POINT = new Point(10, 10);
	static final long TICK_LENGTH = 1000L;
	static final long RANDOM_SEED = 123L;
	static final int NUM_VEHICLES = 200;

	static final int TEST_SPEEDUP = 16;
	static final long TEST_STOP_TIME = 10 * 60 * 1000;

	private MyTestExample() {
	}

	/**
	 * Starts the example.
	 * 
	 * @param args
	 *            This is ignored.
	 */
	public static void main(String[] args) {
		run(false);
	}

	/**
	 * Run the example.
	 * 
	 * @param testing
	 *            if <code>true</code> turns on testing mode.
	 */
	public static void run(boolean testing) {
		// configure the GUI. We use separate renderers for the road model and
		// for the drivers. By default the road model is rendered as a square
		// (indicating its boundaries), and the drivers are rendered as red
		// dots.
		View.Builder viewBuilder = View.builder().with(PlaneRoadModelRenderer.builder())
				.with(RoadUserRenderer.builder());

		if (testing) {
			viewBuilder = viewBuilder.withSpeedUp(TEST_SPEEDUP).withAutoClose().withAutoPlay()
					.withSimulatorEndTime(TEST_STOP_TIME);
		}

		// initialize a new Simulator instance
		final Simulator sim = Simulator.builder()
				// set the length of a simulation 'tick'
				.setTickLength(TICK_LENGTH)
				// set the random seed we use in this 'experiment'
				.setRandomSeed(RANDOM_SEED)
				// add a PlaneRoadModel, a model which facilitates the moving of
				// RoadUsers on a plane. The plane is bounded by two corner points:
				// (0,0) and (10,10)
				.addModel(RoadModelBuilders.plane().withMinPoint(MIN_POINT).withMaxPoint(MAX_POINT)
						.withMaxSpeed(VEHICLE_SPEED_KMH))
				// in case a GUI is not desired simply don't add it.
				.addModel(viewBuilder).build();

		NonMovingObject antHill = new NonMovingObject(sim.getRandomGenerator(), new Point(5, 5));
		sim.register(antHill);		

		// add a number of drivers on the road
		for (int i = 0; i < NUM_VEHICLES; i++) {
			// when an object is registered in the simulator it gets
			// automatically 'hooked up' with models that it's interested in. An
			// object declares to be interested in an model by implementing an
			// interface.
			sim.register(new Ant(sim.getRandomGenerator(), antHill));
		}

		// if a GUI is added, it starts it, if no GUI is specified it will
		// run the simulation without visualization.
		sim.start();
	}

	static class Ant implements MovingRoadUser, TickListener {
		// the MovingRoadUser interface indicates that this class can move on a
		// RoadModel. The TickListener interface indicates that this class wants
		// to keep track of time. The RandomUser interface indicates that this class
		// wants to get access to a random generator

		RoadModel roadModel;
		NonMovingObject antHouse;
		final RandomGenerator rnd;

		Queue<Point> destinations = new LinkedList<>();

		@SuppressWarnings("null")
		Ant(RandomGenerator r, NonMovingObject antHouse) {
			rnd = r;
			this.antHouse = antHouse;
		}

		@Override
		public void initRoadUser(RoadModel model) {
			// this is where we receive an instance to the model. we store the
			// reference and add ourselves to the model on a random position.
			roadModel = model;

		}

		@Override
		public void tick(TimeLapse timeLapse) {
			// every time step (tick) this gets called. Each time we chose a
			// different destination and move in that direction using the time
			// that was made available to us.
			if (!roadModel.containsObject(this)) {
				roadModel.addObjectAt(this, roadModel.getRandomPosition(rnd));
			}
			movement(timeLapse);
		}

		/**
		 * Go to a point until point is actually reached. Then find new point.
		 * 
		 * @param timeLapse
		 */
		private void movement(TimeLapse timeLapse) {

			if (destinations.isEmpty()) {
				destinations.add(roadModel.getRandomPosition(rnd));

				// return home
				destinations.add(roadModel.getPosition(antHouse));
			}

			// destination reached. find new destination
			if (roadModel.getPosition(this).equals(destinations.peek())) {
				destinations.poll();
			}

			if (destinations.isEmpty()) {
				destinations.add(roadModel.getRandomPosition(rnd));

				// return home
				destinations.add(roadModel.getPosition(antHouse));
			}

			roadModel.moveTo(this, destinations.peek(), timeLapse);
		}

		@Override
		public void afterTick(TimeLapse timeLapse) {
			// we don't need this in this example. This method is called after
			// all TickListener#tick() calls, hence the name.
		}

		@Override
		public double getSpeed() {
			// the drivers speed
			return VEHICLE_SPEED_KMH;
		}

	}

	static class NonMovingObject implements MovingRoadUser, TickListener {

		RoadModel roadModel;
		final RandomGenerator rnd;

		Point position;

		@SuppressWarnings("null")
		NonMovingObject(RandomGenerator r, Point pt) {
			rnd = r;
			position = pt;
		}

		@SuppressWarnings("null")
		NonMovingObject(RandomGenerator r) {
			this(r, null);
		}

		@Override
		public void initRoadUser(RoadModel model) {
			// this is where we receive an instance to the model. we store the
			// reference and add ourselves to the model on a random position.
			roadModel = model;
		}

		@Override
		public void tick(TimeLapse timeLapse) {
			if (!roadModel.containsObject(this)) {
				if (position == null) {
					position = roadModel.getRandomPosition(rnd);
				}
				roadModel.addObjectAt(this, position);
			}
		}

		@Override
		public void afterTick(TimeLapse timeLapse) {
			// TODO Auto-generated method stub

		}

		@Override
		public double getSpeed() {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}