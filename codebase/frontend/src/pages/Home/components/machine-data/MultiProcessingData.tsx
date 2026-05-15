import { MultiProcessing } from '@/types/machines';

interface MultiProcessingDataProps {
  machine: MultiProcessing;
}

export function MultiProcessingData({ machine }: MultiProcessingDataProps) {
  const activeCount = [
    machine.ovenInward,
    machine.ovenLight,
    machine.actGripper,
    machine.actRotClockwise,
    machine.actSaw,
    machine.conveyerForward,
    machine.actCompressor,
  ].filter(Boolean).length;

  return (
    <div className="space-y-2 w-full text-xs">
      <div className="flex justify-between">
        <span>Active Components:</span>
        <span className="font-medium">{activeCount}/7</span>
      </div>
      <div className="flex justify-between">
        <span>Oven:</span>
        <span className={machine.ovenLight ? 'text-orange-600' : 'text-gray-600'}>
          {machine.ovenLight ? 'On' : 'Off'}
        </span>
      </div>
      {/* Spacer row to align cards */}
      <div className="flex justify-between">
        <span className="invisible">&nbsp;</span>
        <span className="invisible">&nbsp;</span>
      </div>
    </div>
  );
}
