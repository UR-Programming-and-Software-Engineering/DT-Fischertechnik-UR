import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Settings } from 'lucide-react';
import { formatMachineName } from '@/lib/simulation-utils';

interface SimulationDetailsParametersProps {
  parameters: {
    simulationDays: number;
    simulationHours: number;
    simulationMinutes: number;
    speedMultiplier: number;
    packageGenerationInterval: number;
    machinePowerMultipliers: Record<string, number>;
  };
}

export default function SimulationDetailsParameters({
  parameters,
}: SimulationDetailsParametersProps) {
  return (
    <Card>
      <CardHeader className="pb-4">
        <CardTitle className="flex items-center gap-2">
          <Settings className="w-5 h-5" />
          Simulation Parameters
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Basic Parameters */}
        <div>
          <h3 className="text-sm font-semibold mb-2 text-gray-700">Basic Parameters</h3>
          <div className="flex flex-wrap gap-2">
            <div className="flex items-center gap-2 bg-gray-50 px-3 py-2 rounded-md">
              <span className="text-xs text-gray-600">Simulated Time:</span>
              <span className="text-sm font-medium">
                {parameters.simulationDays}d {parameters.simulationHours}h{' '}
                {parameters.simulationMinutes}m
              </span>
            </div>
            <div className="flex items-center gap-2 bg-gray-50 px-3 py-2 rounded-md">
              <span className="text-xs text-gray-600">Speedup:</span>
              <Badge
                variant="outline"
                className="bg-green-50 text-green-700 border-green-200 text-xs"
              >
                {parameters.speedMultiplier}x
              </Badge>
            </div>
            <div className="flex items-center gap-2 bg-gray-50 px-3 py-2 rounded-md">
              <span className="text-xs text-gray-600">Generation Interval:</span>
              <span className="text-sm font-medium">
                {parameters.packageGenerationInterval}min/package
              </span>
            </div>
          </div>
        </div>

        {/* Machine Power Multipliers */}
        <div>
          <h3 className="text-sm font-semibold mb-2 text-gray-700">Machine Power Multipliers</h3>
          <div className="flex flex-wrap gap-2">
            {Object.entries(parameters.machinePowerMultipliers).map(([machineId, multiplier]) => (
              <div
                key={machineId}
                className="flex items-center gap-2 bg-gray-50 px-3 py-2 rounded-md"
              >
                <span className="text-xs text-gray-600">{formatMachineName(machineId)}:</span>
                <Badge
                  variant="outline"
                  className={`text-xs ${
                    multiplier !== 1.0
                      ? 'bg-yellow-50 text-yellow-700 border-yellow-200'
                      : 'bg-green-50 text-green-700 border-green-200'
                  }`}
                >
                  {multiplier}x
                </Badge>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
