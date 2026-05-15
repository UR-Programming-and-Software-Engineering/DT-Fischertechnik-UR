import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Package, MoveRight, MoveLeft, CornerDownLeft, Play, Goal } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { usePackageStore } from '@/store/packageStore';
import { useState } from 'react';
import { ProductionLineFlowProps } from '@/types/components';
import { useMachineStore } from '@/store/machineStore';

function getFlowStateColor(state: string) {
  switch (state) {
    case 'BUSY':
      return 'border-green-400 bg-green-50';
    case 'FAULT':
      return 'border-red-400 bg-red-50';
    case 'IDLE':
    default:
      return 'border-gray-300 bg-white';
  }
}

function getStateColor(state: string) {
  switch (state) {
    case 'BUSY':
      return 'bg-green-100 text-green-700';
    case 'FAULT':
      return 'bg-red-100 text-red-700';
    case 'IDLE':
    default:
      return 'bg-gray-100 text-gray-700';
  }
}

function MachineStep({ machineName, index }: { machineName: string; index: number }) {
  const { getMachineStepState, getMachineStepColor, getMachineStepBadgeColor } = useMachineStore();
  const MachineStepBadgeColor = getMachineStepBadgeColor(index);
  const machineStepState = getMachineStepState(index);
  //console.log(MachineStepBadgeColor);
  return (
    <div
      className="flex flex-col items-center space-y-2 cursor-pointer"
      onClick={() => console.log('Machine clicked:', machineName)}
    >
      <div
        className={`w-16 h-16 rounded-full border-4 flex items-center justify-center transition-all hover:scale-105 ${getMachineStepColor(
          index
        )}`}
      >
        <Package className="h-6 w-6 text-gray-600" />
      </div>
      <div className="text-center">
        <p className="font-medium text-xs">{machineName}</p>
        <Badge className={`text-xs ${MachineStepBadgeColor}`}>
          {machineStepState.charAt(0) + machineStepState.slice(1).toLowerCase()}
        </Badge>
      </div>
    </div>
  );
}

function StartNode() {
  return (
    <div className="flex flex-col items-center space-y-2">
      <div className="w-16 h-16 rounded-full border-4 border-gray-200 bg-gray-50 flex items-center justify-center transition-all hover:scale-105">
        <Play className="h-6 w-6 text-gray-400" />
      </div>
      <div className="text-center">
        <p className="font-medium text-xs text-gray-500">Start</p>
        {/* Placeholder space to align with machine nodes that have badges */}
        <div className="h-5"></div>
      </div>
    </div>
  );
}

function EndNode() {
  return (
    <div className="flex flex-col items-center space-y-2">
      <div className="w-16 h-16 rounded-full border-4 border-gray-200 bg-gray-50 flex items-center justify-center transition-all hover:scale-105">
        <Goal className="h-6 w-6 text-gray-400" />
      </div>
      <div className="text-center">
        <p className="font-medium text-xs text-gray-500">End</p>
        {/* Placeholder space to align with machine nodes that have badges */}
        <div className="h-5"></div>
      </div>
    </div>
  );
}

export default function ProductionLineFlow({
  title = 'Production Line Flow',
  className = 'col-span-2 md:col-span-4',
}: ProductionLineFlowProps) {
  const { packages, machineSteps } = usePackageStore();

  // Convert machineSteps into our visualization format
  const generateMachineState = (machineIndex: number) => {
    const isProcessing = packages.some((pkg) => pkg.index === machineIndex);
    return isProcessing ? 'BUSY' : 'IDLE';
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-2xl">{title}</CardTitle>
        </div>
      </CardHeader>
      <CardContent>
        {
          <div className="space-y-6">
            {/* First Row - Start node + First 5 machines */}
            <div className="flex items-center justify-center space-x-4">
              <StartNode />
              <MoveRight className="h-6 w-6 text-gray-400 mx-4" />
              {machineSteps.slice(0, 5).map((machineName, index) => (
                <div key={`row1-${machineName}-${index}`} className="flex items-center">
                  <MachineStep
                    machineName={machineName}
                    index={index}
                    //state={generateMachineState(index)}
                  />
                  {index < 4 && <MoveRight className="h-6 w-6 text-gray-400 mx-4" />}
                </div>
              ))}
            </div>

            {/* Second Row - Remaining machines + End node (inverted flow) */}
            {machineSteps.length > 5 && (
              <div className="flex items-center justify-center space-x-4">
                <MoveRight className="h-6 w-6 text-gray-400 mx-4" />

                {machineSteps
                  .slice(5)
                  // .reverse()
                  .map((machineName, index) => (
                    <div key={`row2-${machineName}-${index}`} className="flex items-center">
                      <MachineStep
                        machineName={machineName}
                        index={index + 5}
                        //state={generateMachineState(index + 5)}
                      />
                      {index < machineSteps.slice(5).length - 1 && (
                        <MoveRight className="h-6 w-6 text-gray-400 mx-4" />
                      )}
                    </div>
                  ))}
                <MoveRight className="h-6 w-6 text-gray-400 mx-4" />
                <EndNode />
                {/* <CornerDownLeft className="h-6 w-6 text-gray-400 mx-4" /> */}
              </div>
            )}

            {/* If there are 5 or fewer machines, add end node to first row */}
            {machineSteps.length <= 5 && (
              <div className="flex items-center justify-center">
                <MoveRight className="h-6 w-6 text-gray-400 mx-4" />
                <EndNode />
              </div>
            )}
          </div>
        }
      </CardContent>
    </Card>
  );
}
