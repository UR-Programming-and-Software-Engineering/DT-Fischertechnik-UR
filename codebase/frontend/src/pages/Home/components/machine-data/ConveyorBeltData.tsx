import { ConveyorBelt } from '@/types/machines';

interface ConveyorBeltDataProps {
  machine: ConveyorBelt;
}

export function ConveyorBeltData({ machine }: ConveyorBeltDataProps) {
  return (
    <div className="space-y-2 w-full text-xs">
      <div className="flex justify-between">
        <span>Direction:</span>
        <span className="font-medium">{machine.forward ? 'Forward' : 'Backward'}</span>
      </div>
      <div className="flex justify-between">
        <span>Conveyor:</span>
        <span className={machine.conveyorRunning ? 'text-green-600' : 'text-gray-600'}>
          {machine.conveyorRunning ? 'Running' : 'Stopped'}
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
