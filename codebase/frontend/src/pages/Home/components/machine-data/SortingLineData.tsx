import { SortingLine } from '@/types/machines';

interface SortingLineDataProps {
  machine: SortingLine;
}

export function SortingLineData({ machine }: SortingLineDataProps) {
  return (
    <div className="space-y-2 w-full text-xs">
      <div className="flex justify-between">
        <span>Conveyor:</span>
        <span className={machine.conveyorRunning ? 'text-green-600' : 'text-gray-600'}>
          {machine.conveyorRunning ? 'Running' : 'Stopped'}
        </span>
      </div>
      {machine.colorPresent && (
        <div className="flex justify-between">
          <span>Color:</span>
          <span className="font-medium">{machine.colorPresent}</span>
        </div>
      )}
      {machine.ejectedToBranch && (
        <div className="flex justify-between">
          <span>Branch:</span>
          <span className="font-medium">{machine.ejectedToBranch}</span>
        </div>
      )}
    </div>
  );
}
