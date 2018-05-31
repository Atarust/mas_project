package mas_project;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Metric {

	private int passengersDelivered;
	private int passengersSpawned;
	private int numMessagesSent;
	private int numNewParcelsComm;
	private int ticksSpentIdle;
	private int ticks; // ticks = ticks*nrOfAgents
	private List<Long> numParcelsKnownPerTick;
	private List<Long> waitingTimePassenger; // If passenger is not picked up he is not counted. easier to implement
	private Set<Parcel> gotDelivered;
	private double numParcelsKnownPerTickAverage;
	private double waitingTimePassengerAverage;

	public Metric() {
		passengersDelivered = 0;
		passengersSpawned = 0;

		numMessagesSent = 0;
		numNewParcelsComm = 0;
		ticksSpentIdle = 0;
		ticks = 0;

		numParcelsKnownPerTick = new LinkedList<>();
		waitingTimePassenger = new LinkedList<>();
		gotDelivered = new HashSet<>();

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
		numParcelsKnownPerTickAverage = numParcelsKnownPerTick.stream().mapToDouble(a -> a).average().orElse(-1);
	}

	public void parcelWaitingTime(long l, Parcel parcel) {
		if (!gotDelivered.contains(parcel)) {
			waitingTimePassenger.add(l);
			waitingTimePassengerAverage = waitingTimePassenger.stream().mapToDouble(a -> a).average().orElse(-1);
			gotDelivered.add(parcel);
		}
	}

	public int getResult() {
		// return passengersDelivered;
		return -1;
	}

	public static String csvHeader() {
		return "passengersDelivered,passengersSpawned,numMessagesSent,numNewParcelsComm,ticksSpentIdle,ticks,numParcelsKnownPerTickAverage,waitingTimePassengerAverage";
	}

	public List<Object> toCSV() {
		return Arrays.asList(new Object[] { passengersDelivered, passengersSpawned, numMessagesSent, numNewParcelsComm,
				ticksSpentIdle, ticks, numParcelsKnownPerTickAverage, waitingTimePassengerAverage });
	}

}
