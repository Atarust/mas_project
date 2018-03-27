package warehouse;

import java.util.LinkedList;
import java.util.Queue;

import javax.measure.unit.SystemOfUnits;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.DeadlockException;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;

class MyAGVAgent implements TickListener, MovingRoadUser {
	private final RandomGenerator rng;
	private Optional<CollisionGraphRoadModelImpl> roadModel;
	private Optional<Point> destination;
	private Queue<Point> path;

	MyAGVAgent(RandomGenerator r) {
		rng = r;
		roadModel = Optional.absent();
		destination = Optional.absent();
		path = new LinkedList<>();
	}

	@Override
	public void initRoadUser(RoadModel model) {
		roadModel = Optional.of((CollisionGraphRoadModelImpl) model);
		Point p;
		do {
			p = model.getRandomPosition(rng);
		} while (roadModel.get().isOccupied(p));
		roadModel.get().addObjectAt(this, p);

	}

	@Override
	public double getSpeed() {
		return 1;
	}

	void nextDestination() {
		destination = Optional.of(roadModel.get().getRandomPosition(rng));
		path = new LinkedList<>(roadModel.get().getShortestPathTo(this, destination.get()));
	}

	void changePath() {
		boolean foundFreeRoad = false;
		int tries = 0;
		while(!foundFreeRoad) {
			Point via = roadModel.get().getRandomPosition(rng);
			path = new LinkedList<>(roadModel.get().getShortestPathTo(this, via));
			if(!roadModel.get().isOccupied(path.peek())) {
				foundFreeRoad = true;
			}
			tries++;
			
			if (tries > 10000) {
				System.out.println("max tries reached. just stop moving.");
				path.clear();
				break;
			}
		}
		
		
	}

	@Override
	public void tick(TimeLapse timeLapse) {
		if (!destination.isPresent()) {
			nextDestination();
		}
		
		if(path.isEmpty()) {
			path = new LinkedList<>(roadModel.get().getShortestPathTo(this, destination.get()));
		}

		try {
			if (!path.isEmpty()) {
				roadModel.get().followPath(this, path, timeLapse);
			}
		} catch (DeadlockException e) {
			//path.clear(); // just stop
			changePath();
			System.out.println(this.hashCode() + "changing path.");
		}

		/*
		Point dest = path.peek();
		if (roadModel.get().isOccupied(dest)) {
			// can't go to next node, change!
			changePath();
		}
		roadModel.get().followPath(this, path, timeLapse);
		*/

		if (roadModel.get().getPosition(this).equals(destination.get())) {
			nextDestination();

		}
	}

	@Override
	public void afterTick(TimeLapse timeLapse) {
	}

}