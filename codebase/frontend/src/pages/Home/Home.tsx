import ClickableCard from '@/pages/Home/components/ClickableCard';
import { useMachineStore } from '@/store/machineStore';
import { SortingLineData } from '@/pages/Home/components/machine-data/SortingLineData';
import { ConveyorBeltData } from '@/pages/Home/components/machine-data/ConveyorBeltData';
import { VacuumGripperData } from '@/pages/Home/components/machine-data/VacuumGripperData';
import { HighBayData } from '@/pages/Home/components/machine-data/HighBayData';
import { MultiProcessingData } from '@/pages/Home/components/machine-data/MultiProcessingData';
import ProcessSVG from '@/pages/Home/components/processSVG';
import { useSimulationStore } from '@/store/simulationStore';

// Helper function to get machine type from name (remove last 2 characters)
function getMachineTypeFromName(machineName: string): string {
  return machineName.slice(0, -2);
}

export default function Home() {
  const { machines } = useMachineStore();
  const { currentSimulationDetails } = useSimulationStore();
  const speedMultiplier = currentSimulationDetails?.parameters?.speedMultiplier || 1;
  // Get all machines as array
  const allMachines = Object.values(machines);

  const renderMachineCard = (machine: any, index: number) => {
    const machineType = getMachineTypeFromName(machine.machineName);

    let dataComponent = null;

    switch (machineType) {
      case 'SortingLine':
        dataComponent = <SortingLineData machine={machine} />;
        break;
      case 'ConveyorBelt':
        dataComponent = <ConveyorBeltData machine={machine} />;
        break;
      case 'VacuumGripper':
        dataComponent = <VacuumGripperData machine={machine} />;
        break;
      case 'HighBay':
        dataComponent = <HighBayData machine={machine} />;
        break;
      case 'MultiProcessing':
        dataComponent = <MultiProcessingData machine={machine} />;
        break;
      default:
        dataComponent = <div className="text-xs text-gray-500">No data available</div>;
    }

    return (
      <ClickableCard
        key={machine.machineName}
        name={machine.machineName}
        index={index + 1}
        state={machine.state}
        currentOverload={machine.currentOverload ? machine.currentOverload : 0}
        overheatingCounter={machine.overheatingCounter ? machine.overheatingCounter : 0}
        speedMultiplier={speedMultiplier}
      >
        {dataComponent}
      </ClickableCard>
    );
  };

  const renderPlaceholder = (index: number) => (
    <div
      key={`placeholder-${index}`}
      className="w-70 h-70 bg-gray-200 rounded-lg border border-gray-300 opacity-60 flex items-center justify-center"
    >
      <span className="text-gray-500 text-sm">Empty Slot</span>
    </div>
  );

  // Create grid items (8 slots total)
  const totalSlots = 8;
  const gridItems = [];

  allMachines.forEach((machine, index) => {
    if (index < totalSlots) {
      gridItems.push(renderMachineCard(machine, index));
    }
  });

  // Fill remaining slots with placeholders
  for (let i = allMachines.length; i < totalSlots; i++) {
    gridItems.push(renderPlaceholder(i));
  }

  return (
    <div className="min-h-screen bg-gray-100 px-4">
      <div className="flex flex-col items-center justify-center pt-8">
        {/* Production Flow Heading */}
        <div className="w-full max-w-7xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Production Flow Visualization</h2>
        </div>

        {/* ProcessSVG component above the cards */}
        <div className="w-full max-w-7xl mb-8">
          <ProcessSVG />
        </div>

        {/* Machine Overview Heading */}
        <div className="w-full max-w-7xl mb-6">
          <h2 className="text-2xl font-semibold text-gray-800">Machine Status Dashboard</h2>
        </div>

        {/* Card grid */}
        <div className="grid grid-cols-4 grid-rows-2 gap-4 max-w-7xl mb-20">{gridItems}</div>
      </div>
    </div>
  );
}
