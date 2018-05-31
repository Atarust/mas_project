package mas_project;

import java.util.LinkedList;
import java.util.List;

public class Metric {

	private int passengersDelivered;
	private int passengersSpawned;
	private int numMessagesSent;
	private int numNewParcelsComm;
	private int ticksSpentIdle;
	private int ticks;
	private List<Long> numParcelsKnownPerTick;

	public Metric() {
		passengersDelivered = 0;
		passengersSpawned = 0;

		numMessagesSent = 0;
		numNewParcelsComm = 0;
		ticksSpentIdle = 0;
		ticks = 0;

		numParcelsKnownPerTick = new LinkedList<>();
	}

	public void passengerDelivered() {
		passengersDelivered++;
	}

	public void passengerSpawned(int passengersSpawned) {
		this.passengersSpawned = passengersSpawned;
	}

	public void messageSent() {
		numMessagesSent++;
	}

	public void newParcelCommunicated(int newParcels) {
		numNewParcelsComm += Math.abs(newParcels);
	}

	public void countTick() {
		// Was idle at beginning and idle at end. So a whole tick was idle.
		ticks++;
	}

	public void spentIdle() {
		// Was idle at beginning and idle at end. So a whole tick was idle.
		ticksSpentIdle++;
	}

	public void newParcelsKnown(long l) {
		numParcelsKnownPerTick.add(l);
	}

	public int getResult() {
		// return passengersDelivered;
		return -1;
	}

	@Override
	public String toString() {
		double numParcelsKnownPerTickAverage = numParcelsKnownPerTick.stream().mapToDouble(a -> a).average().orElse(-1);

		return "[passengersDelivered:" + passengersDelivered + "]" + "[passengersSpawned:" + passengersSpawned + "]"
				+ "[numMessagesSent:" + numMessagesSent + "]" + "[numNewParcelsComm:" + numNewParcelsComm + "]"
				+ "[ticksSpentIdle:" + ticksSpentIdle + "]" + "[ticks:" + ticks + "]" + "[numParcelsKnown:"
				+ numParcelsKnownPerTickAverage + "]";
	}

}
