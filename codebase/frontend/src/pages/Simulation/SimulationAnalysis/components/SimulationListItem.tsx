import { Badge } from '@/components/ui/badge';
import {
  formatSimulationDate,
  getSpeedDescription,
  getParameterStatus,
  formatDurationFromMs,
} from '@/lib/simulation-utils';
import { Clock } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { SimulationListItemProps } from '@/types/components';

export default function SimulationListItem({ simulation, onClick }: SimulationListItemProps) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (onClick) {
      onClick(simulation.id);
    } else {
      // Default navigation - you can customize this route
      navigate(`/simulation-analysis/${simulation.id}`);
    }
  };

  return (
    <div
      className="grid grid-cols-12 gap-4 py-3 border border-gray-200 rounded-lg hover:bg-gray-50 hover:border-blue-300 transition-all cursor-pointer"
      onClick={handleClick}
    >
      {/* Simulation Name */}
      <div className="col-span-3 px-4 flex items-center">
        <span className="font-medium text-gray-900 hover:text-blue-600 transition-colors">
          {simulation.name}
        </span>
      </div>

      {/* Parameters Badges */}
      <div className="col-span-4 px-2 flex items-center gap-2 flex-wrap">
        {(() => {
          const allBadges = [
            <Badge
              key="speed"
              variant="outline"
              className="bg-green-50 text-green-700 border-green-200"
            >
              {getSpeedDescription(simulation.parameters.speedMultiplier)}
            </Badge>,
            ...getParameterStatus(simulation.parameters).map((status, index) => (
              <Badge key={`param-${index}`} variant="secondary" className="text-xs">
                {status}
              </Badge>
            )),
          ];

          const displayBadges = allBadges.slice(0, 3);
          if (allBadges.length > 3) {
            displayBadges.push(
              <Badge key="more" variant="secondary" className="text-xs">
                ...
              </Badge>
            );
          }

          return displayBadges;
        })()}
      </div>

      {/* Date & Time */}
      <div className="col-span-3 px-2 flex items-center text-sm text-gray-600">
        <Clock className="w-4 h-4 mr-2 text-gray-400" />
        {formatSimulationDate(new Date(simulation.timeCreated))}
      </div>

      {/* Duration */}
      <div className="col-span-2 px-4 flex items-center text-sm text-gray-600">
        {formatDurationFromMs(simulation.duration)}
      </div>
    </div>
  );
}
