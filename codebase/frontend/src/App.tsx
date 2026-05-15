import { Route, Routes } from 'react-router-dom';
import Home from './pages/Home/Home';
import Header from './components/header';
import { useEffect, useRef } from 'react';
import { useMachineStore } from './store/machineStore';
import { usePackageStore } from './store/packageStore';
import { useSimulationStore } from './store/simulationStore';
import SimulationControl from './pages/Simulation/SimulationControl/SimulationControl';
import PackageTracking from './pages/PackageTracking/PackageTracking';
import SimulationAnalysis from './pages/Simulation/SimulationAnalysis/SimulationAnalysis';
import SimulationDetails from './pages/Simulation/SimulationDetails/SimulationDetails';
import { simulationService } from './services/simulationService';

function App() {
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectIntervalRef = useRef(1000);
  const reconnectTimeoutRef = useRef<number | null>(null);

  // Store Actions für Updates
  const { updateMachines } = useMachineStore();
  const { updatePackages, setMachineSteps } = usePackageStore();

  // Load machine steps on app start
  useEffect(() => {
    const loadMachineSteps = async () => {
      try {
        const machineSteps = await simulationService.getMachineSteps();
        setMachineSteps(machineSteps);
      } catch (error) {
        console.error('Error loading machine steps:', error);
      }
    };

    loadMachineSteps();
  }, [setMachineSteps]);

  const connectWebSocket = () => {
    const ws = new WebSocket('ws://localhost:8080/ws');
    wsRef.current = ws;

    ws.onopen = () => {
      console.log('WebSocket verbunden');
      reconnectIntervalRef.current = 1000;
      ws.send('Hello from client');
    };

    ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        //console.log('WebSocket message received:', data);

        // Handle simulation running status
        if (typeof data.simulationRunning === 'boolean') {
          useSimulationStore.getState().setSimulationRunning(data.simulationRunning);
        }

        // Handle machine updates with enhanced data structure for simulations
        if (Array.isArray(data)) {
          updateMachines(data);
        } else if (data.machines && Array.isArray(data.machines)) {
          // When simulation is running, machines will include energy usage data
          updateMachines(data.machines);
        } else {
          updateMachines([data]);
        }

        // Handle package updates
        if (data.packages && Array.isArray(data.packages)) {
          updatePackages(data.packages);
        }
      } catch (error) {
        console.error('Fehler beim Parsen der WebSocket-Nachricht:', error);
      }
    };

    ws.onclose = () => {
      console.log(`WebSocket getrennt, reconnect in ${reconnectIntervalRef.current}ms`);
      reconnectTimeoutRef.current = window.setTimeout(() => {
        connectWebSocket();
      }, reconnectIntervalRef.current);
      reconnectIntervalRef.current = Math.max(60000, reconnectIntervalRef.current * 2);
    };

    ws.onerror = (error) => {
      console.error('WebSocket Fehler:', error);
    };
  };

  useEffect(() => {
    connectWebSocket();
    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current?.readyState === WebSocket.OPEN) {
        wsRef.current.close();
      }
    };
  }, [updateMachines, updatePackages, setMachineSteps]);

  return (
    <>
      <Header />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/package-tracking" element={<PackageTracking />} />
        <Route path="/simulation-control" element={<SimulationControl />} />
        <Route path="/simulation-analysis" element={<SimulationAnalysis />} />
        <Route path="/simulation-analysis/:id" element={<SimulationDetails />} />
      </Routes>
    </>
  );
}

export default App;
