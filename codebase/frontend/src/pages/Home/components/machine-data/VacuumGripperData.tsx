import { VacuumGripper } from '@/types/machines';

interface VacuumGripperDataProps {
  machine: VacuumGripper;
}

export function VacuumGripperData({ machine }: VacuumGripperDataProps) {
  return (
    <div className="space-y-2 w-full text-xs">
      <div className="flex justify-between">
        <span>Rotation:</span>
        <span className="font-medium">{machine.rotation.toFixed(1)}°</span>
      </div>
      <div className="flex justify-between">
        <span>Height:</span>
        <span className="font-medium">{machine.height}mm</span>
      </div>
      <div className="flex justify-between">
        <span>Forward:</span>
        <span className="font-medium">{machine.forward}mm</span>
      </div>
    </div>
  );
}
