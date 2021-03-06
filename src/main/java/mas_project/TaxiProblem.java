/*
 * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mas_project;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.comm.CommModel;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.event.Listener;
//import com.github.rinde.rinsim.examples.core.taxi.TaxiRenderer.Language;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.MultiAttributeData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.io.DotGraphIO;
import com.github.rinde.rinsim.geom.io.Filters;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.CommRenderer;
import com.github.rinde.rinsim.ui.renderers.GraphRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;

/**
 * Example showing a fleet of taxis that have to pickup and transport customers
 * around the city of Leuven.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * 
 * @author Jonas Kapitzke, Doruk Dündar
 */
public final class TaxiProblem {

	private TaxiProblem() {
	}
	
	public static void main(String[] args) {
		Parameter p = Parameter.defaultParameter();
		p.withGui(true);
		p.withTesting(false);
		run(p);
	}
	
	public static Simulator run(Parameter parameter) {

		final View.Builder view = createGui(parameter.testing, parameter, Parameter.display, Parameter.m, Parameter.list);

		final Simulator simulator;
		if (parameter.gui) {
			simulator = Simulator.builder()
					.addModel(RoadModelBuilders.staticGraph(loadGraph(Parameter.graphFile)))
					.addModel(DefaultPDPModel.builder()).addModel(CommModel.builder())
					.setRandomSeed(parameter.RANDOM_SEED).addModel(view).build();
		} else {
			simulator = Simulator.builder()
					.addModel(RoadModelBuilders.staticGraph(loadGraph(Parameter.graphFile)))
					.addModel(DefaultPDPModel.builder()).addModel(CommModel.builder())
					.setRandomSeed(parameter.RANDOM_SEED)
					// .addModel(view)
					.build();
		}
		final RandomGenerator rng = simulator.getRandomGenerator();

		final RoadModel roadModel = simulator.getModelProvider().getModel(RoadModel.class);
		// add depots, taxis and parcels to simulator
		for (int i = 0; i < Parameter.NUM_DEPOTS; i++) {
			simulator.register(new TaxiBase(roadModel.getRandomPosition(rng), Parameter.DEPOT_CAPACITY));
		}
		for (int i = 0; i < parameter.numTaxis; i++) {
			simulator.register(new TaxiImplDetails(parameter, roadModel.getRandomPosition(rng), rng));
		}
		for (int i = 0; i < parameter.numCustomers; i++) {
			simulator.register(new Customer(Parcel
					.builder(roadModel.getRandomPosition(rng), roadModel.getRandomPosition(rng))
					.serviceDuration(Parameter.SERVICE_DURATION).neededCapacity(1 + rng.nextInt(Parameter.MAX_CAPACITY)).buildDTO()));
		}

		simulator.addTickListener(new TickListener() {
			@Override
			public void tick(TimeLapse time) {
				if (time.getStartTime() > parameter.runTime) {
					simulator.stop();
					Experiment.experimentStoppedListener(parameter.metric);
				} else if (rng.nextDouble() < parameter.newCustomerProb) {
					simulator.register(new Customer(
							Parcel.builder(roadModel.getRandomPosition(rng), roadModel.getRandomPosition(rng))
									.serviceDuration(Parameter.SERVICE_DURATION).neededCapacity(1 + rng.nextInt(Parameter.MAX_CAPACITY))
									.buildDTO()));
				}
			}

			@Override
			public void afterTick(TimeLapse timeLapse) {
			}
		});
		simulator.start();

		return simulator;

	}
	static View.Builder createGui(boolean testing, Parameter parameter, @Nullable Display display, @Nullable Monitor m,
			@Nullable Listener list) {

		
		
		View.Builder view;
		boolean renderComm = parameter.commRange < 100000;
		if(renderComm) {
			view = View.builder().with(GraphRoadModelRenderer.builder())
					.with(RoadUserRenderer.builder()
							.withImageAssociation(TaxiBase.class, "/graphics/perspective/tall-building-64.png")
							.withImageAssociation(TaxiImplDetails.class, "/graphics/flat/taxi-32.png")
							.withImageAssociation(Customer.class, "/graphics/flat/person-red-32.png"))
					// .with(TaxiRenderer.builder(Language.ENGLISH))
					.withTitleAppendix("Taxi Demo")
			 .with(CommRenderer.builder()
			 .withReliabilityColors()
			 .withToString()
			 .withMessageCount());
		} else {
			view = View.builder().with(GraphRoadModelRenderer.builder())
					.with(RoadUserRenderer.builder()
							.withImageAssociation(TaxiBase.class, "/graphics/perspective/tall-building-64.png")
							.withImageAssociation(TaxiImplDetails.class, "/graphics/flat/taxi-32.png")
							.withImageAssociation(Customer.class, "/graphics/flat/person-red-32.png"))
					// .with(TaxiRenderer.builder(Language.ENGLISH))
					.withTitleAppendix("Taxi Demo");
		 }

		if (testing) {
			view = view.withAutoClose().withAutoPlay().withSimulatorEndTime(Parameter.TEST_STOP_TIME).withSpeedUp(Parameter.TEST_SPEED_UP);
		} else if (m != null && list != null && display != null) {
			view = view.withMonitor(m).withSpeedUp(Parameter.SPEED_UP)
					.withResolution(m.getClientArea().width, m.getClientArea().height).withDisplay(display)
					.withCallback(list).withAsync().withAutoPlay().withAutoClose();
		}
		return view;
	}

	// load the graph file
	static Graph<MultiAttributeData> loadGraph(String name) {
		try {
			if (Parameter.GRAPH_CACHE.containsKey(name)) {
				return Parameter.GRAPH_CACHE.get(name);
			}
			final Graph<MultiAttributeData> g = DotGraphIO.getMultiAttributeGraphIO(Filters.selfCycleFilter())
					.read(TaxiProblem.class.getResourceAsStream(name));

			Parameter.GRAPH_CACHE.put(name, g);
			return g;
		} catch (final FileNotFoundException e) {
			throw new IllegalStateException(e);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * A customer with very permissive time windows.
	 */
	static class Customer extends Parcel {
		Customer(ParcelDTO dto) {
			super(dto);
		}

		@Override
		public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		}
	}

	// currently has no function
	static class TaxiBase extends Depot {
		TaxiBase(Point position, double capacity) {
			super(position);
			setCapacity(capacity);
		}

		@Override
		public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
		}
	}

}
