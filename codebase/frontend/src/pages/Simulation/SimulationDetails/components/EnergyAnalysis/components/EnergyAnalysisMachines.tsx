import { Settings } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

import { EnergyAnalysisMachinesProps } from '@/types/energy';

export default function EnergyAnalysisMachines(props: EnergyAnalysisMachinesProps) {
  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Settings className="w-5 h-5 text-blue-500" />
            Machine Energy Analysis
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            {/* Header Row */}
            <div className="grid grid-cols-4 gap-4 p-3 bg-gray-100 rounded-lg font-medium text-sm text-gray-700">
              <div>Machine Name</div>
              <div>Wh Consumption</div>
              <div>% of Total</div>
              <div>Energy-Per-Package (Wh)</div>
            </div>

            {/* Data Rows */}
            {props.enrichedMachineData.map((machine) => (
              <div
                key={machine.machine}
                className="grid grid-cols-4 gap-4 p-3 bg-gray-50 rounded-lg items-center"
              >
                <div className="font-medium text-gray-900">
                  {props.formatMachineName(machine.machine)}
                </div>
                <div className="text-gray-700">
                  <span className="font-semibold">{machine.consumption}</span> Wh
                </div>
                <div className="text-gray-700">
                  <span className="font-semibold">{machine.percentageOfTotal}%</span>
                </div>
                <div className="text-gray-700">
                  <span className="font-semibold">{machine.energyPerPackage}</span> Wh
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
