import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Settings } from 'lucide-react';
import { BaseSimulationData } from '@/types/simulation';
import SimulationListItem from '@/pages/Simulation/SimulationAnalysis/components/SimulationListItem';

interface SimulationExplorerProps {
  simulations?: BaseSimulationData[];
}

export default function SimulationExplorer({ simulations = [] }: SimulationExplorerProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="w-5 h-5" />
          Past Simulations
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Header row */}
        <div className="grid grid-cols-12 gap-4 pb-3 mb-4 border-b border-gray-200 text-sm font-medium text-gray-600">
          <div className="col-span-3">Simulation Name</div>
          <div className="col-span-4">Parameters</div>
          <div className="col-span-3">Date & Time</div>
          <div className="col-span-2">Duration</div>
        </div>

        {/* Simulation list */}
        <div className="space-y-3">
          {simulations.map((simulation) => (
            <SimulationListItem
              key={simulation.id}
              simulation={simulation}
              //   onClick={handleSimulationClick} // Optional custom handler
            />
          ))}
        </div>

        {/* Empty state */}
        {simulations.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            <Settings className="w-12 h-12 mx-auto mb-4 text-gray-300" />
            <p>No past simulations found</p>
            <p className="text-sm">Start a new simulation to see it here</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
