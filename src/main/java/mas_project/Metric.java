package mas_project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.rinde.rinsim.core.model.pdp.Parcel;

public class Metric {

	private int passengersDelivered;
	private int passengersSpawned;
	private int numMessagesSent;
	private int numNewParcelsComm;
	private int ticksTaxiSpentIdle;
	private int ticks; // ticks = ticks*nrOfAgents
	private List<Long> numParcelsKnownPerTick;
	private List<Long> waitingTimePassenger; // If passenger is not picked up he is not counted. easier to implement
	private List<Long> numParcelsAvailablePerTick;
	private Set<Parcel> gotDelivered;
	private int nrOfLazy;

	private Map<Parcel, Long> seenTime;
	private Map<Parcel, Long> pickedTime;
	private List<Long> seenPickTimes;
	private double ticksBetweenSeenAndPickedAverage;

	/*
	 * How many Parcels a taxi knows. If it knows if 5 passengers in one tick and in
	 * the next tick it learns of two more, the average is 6.
	 */
	private double numParcelsKnownPerTickAverage;
	private double numParcelsSeenPerTickAverage; // Parcels need not to be new.
	private double numParcelsAvailablePerTickAverage;

	private double waitingTimePassengerAverage;

	public Metric() {
		passengersDelivered = 0;
		passengersSpawned = 0;

		numMessagesSent = 0;
		numNewParcelsComm = 0;
		ticksTaxiSpentIdle = 0;
		ticks = 0;
		nrOfLazy = 0;

		numParcelsKnownPerTick = new LinkedList<>();
		numParcelsAvailablePerTick = new LinkedList<>();
		seenPickTimes = new LinkedList<>();
		seenTime = new HashMap<>();
		pickedTime = new HashMap<>();
		
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

	public void isLazy() {
		nrOfLazy++;
	}
	
	public void spentIdle() {
		// Was idle at beginning and idle at end. So a whole tick was idle.
		ticksTaxiSpentIdle++;
	}

	public void newParcelsKnown(long l) {
		numParcelsKnownPerTick.add(l);
		numParcelsKnownPerTickAverage = numParcelsKnownPerTick.stream().mapToDouble(a -> a).average().orElse(-1);
	}

	public void newParcelsSeen(Parcel p, long tick) {
		if (!seenTime.containsKey(p)) {
			seenTime.put(p, tick);
		}
	}

	public void parcelPicked(Parcel p, long tick) {
		if (!pickedTime.containsKey(p)) {
			pickedTime.put(p, tick);
		}

		// calculate time that was needed to pick that guy up:
		if (seenTime.containsKey(p) && pickedTime.containsKey(p)) {
			seenPickTimes.add(pickedTime.get(p) - seenTime.get(p));
			ticksBetweenSeenAndPickedAverage = seenPickTimes.stream().mapToLong(l -> l).average().orElse(0);
		} else {
			System.out.println("dafuqq?");
		}
	}

	public void numParcelsAvailable(long l) {
		numParcelsAvailablePerTick.add(l);
		numParcelsAvailablePerTickAverage = numParcelsAvailablePerTick.stream().mapToDouble(a -> a).average()
				.orElse(-1);
		//System.out.println(numParcelsAvailablePerTickAverage);
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
		return "passengersDelivered,passengersSpawned,numMessagesSent,numNewParcelsComm,ticksTaxiSpentIdle,ticks,numParcelsKnownPerTickAverage,numParcelsAvailablePerTickAverage,waitingTimePassengerAverage,ticksBetweenSeenAndPickedAverage,nrOfLazy";
	}

	public List<Object> toCSV() {
		return Arrays.asList(new Object[] { passengersDelivered, passengersSpawned, numMessagesSent, numNewParcelsComm,
				ticksTaxiSpentIdle, ticks, numParcelsKnownPerTickAverage, numParcelsAvailablePerTickAverage,
				waitingTimePassengerAverage, ticksBetweenSeenAndPickedAverage, nrOfLazy });
	}

}
