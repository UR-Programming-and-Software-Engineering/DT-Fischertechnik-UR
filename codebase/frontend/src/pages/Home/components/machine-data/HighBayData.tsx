import { HighBay } from '@/types/machines';

interface HighBayDataProps {
  machine: HighBay;
}

export function HighBayData({ machine }: HighBayDataProps) {
  return (
    <div className="space-y-2 w-full text-xs">
      <div className="flex justify-between">
        <span>Position:</span>
        <span className="font-medium">
          H{machine.horizontal}/V{machine.height}
        </span>
      </div>
      <div className="flex justify-between">
        <span>Conveyor:</span>
        <span className="font-medium">
          {machine.conveyorForward ? 'Forward' : machine.conveyorBackward ? 'Backward' : 'Stop'}
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
