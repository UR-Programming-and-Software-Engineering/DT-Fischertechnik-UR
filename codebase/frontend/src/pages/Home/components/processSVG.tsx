// Import individual SVG components
import SortingLine01SVG from '../../../components/svgs/SortingLine01SVG';
import VacuumGripper01SVG from '../../../components/svgs/VacuumGripper01SVG';
import VacuumGripper02SVG from '../../../components/svgs/VacuumGripper02SVG';
import ConveyorBelt01SVG from '../../../components/svgs/ConveyorBelt01SVG';
import MultiProcessing01SVG from '../../../components/svgs/MultiProcessing01SVG';
import HighBay01SVG from '../../../components/svgs/Highbay01SVG';
import Circle01SVG from '../../../components/svgs/Circle01SVG';
import { usePackageStore } from '@/store/packageStore';
import { Package } from 'lucide-react';
import Circle02SVG from '../../../components/svgs/Circle02SVG';
import PackageInSVG from '../../../components/svgs/PackageInSVG';
import StorageSVG from '../../../components/svgs/StorageSVG';
import { useMachineStore } from '@/store/machineStore';

// Define background circles (no packages)
const backgroundElements = [
  {
    name: 'Circle01',
    component: Circle01SVG,
    transform: 'scale(0.7) translate(-420, 400)',
  },
  {
    name: 'Circle02',
    component: Circle02SVG,
    transform: 'scale(0.77) translate(680, 280)',
  },
  {
    name: 'PackageIn',
    component: PackageInSVG,
    transform: 'scale(0.15) translate(-1300, 1000)',
  },
  {
    name: 'Storage',
    component: StorageSVG,
    transform: 'scale(0.075) translate(22000, 3200)',
  },
];

// Define machine components with their names and package positions
const machineComponents = [
  {
    name: 'SortingLine01',
    component: SortingLine01SVG,
    transform: 'scale(0.5) translate(0, 0)',
    packagePosition: { x: 430, y: 380 },
  },
  {
    name: 'VacuumGripper01',
    component: VacuumGripper01SVG,
    transform: 'scale(0.6) translate(1800, 700)',
    packagePosition: { x: 1430, y: 750 },
  },
  {
    name: 'VacuumGripper02',
    component: VacuumGripper02SVG,
    transform: 'scale(0.6) translate(100, 850)',
    packagePosition: { x: 410, y: 850 },
  },
  {
    name: 'ConveyorBelt01',
    component: ConveyorBelt01SVG,
    transform: 'scale(0.45) translate(1000, 700)',
    packagePosition: { x: 700, y: 550 },
  },
  {
    name: 'MultiProcessing01',
    component: MultiProcessing01SVG,
    transform: 'scale(0.5) translate(2000, -200)',
    packagePosition: { x: 1280, y: 250 },
  },
  {
    name: 'HighBay01',
    component: HighBay01SVG,
    transform: 'scale(0.6) translate(2300, 400)',
    packagePosition: { x: 1720, y: 450 },
  },
];

// Create a mapping from machine name to package position
const machinePackagePositions: Record<string, { x: number; y: number }> = machineComponents.reduce(
  (acc, machine) => {
    acc[machine.name] = machine.packagePosition;
    return acc;
  },
  {} as Record<string, { x: number; y: number }>
);

export default function ProcessSVG() {
  const { packages } = usePackageStore();
  const { getMachineColor } = useMachineStore();

  const packageColors = [
    { fill: '#ef4444', stroke: '#dc2626' }, // Red
    { fill: '#3b82f6', stroke: '#2563eb' }, // Blue
    { fill: '#10b981', stroke: '#059669' }, // Green
    { fill: '#f59e0b', stroke: '#d97706' }, // Orange
    { fill: '#8b5cf6', stroke: '#7c3aed' }, // Purple
    { fill: '#ec4899', stroke: '#db2777' }, // Pink
    { fill: '#06b6d4', stroke: '#0891b2' }, // Cyan
    { fill: '#84cc16', stroke: '#65a30d' }, // Lime
  ];

  // Get positions for all packages
  const getPackagePositions = () => {
    return packages.map((pkg, index) => {
      const position = machinePackagePositions[pkg.machineName];
      const colorIndex = index % packageColors.length;

      if (position) {
        return {
          ...position,
          packageId: pkg.id,
          machineName: pkg.machineName,
          color: packageColors[colorIndex],
        };
      }

      // Fallback to default if machine not found
      return {
        x: 100 + index * 30,
        y: 100 + index * 15,
        packageId: pkg.id,
        machineName: pkg.machineName,
        color: packageColors[colorIndex],
      };
    });
  };

  const packagePositions = getPackagePositions();

  return (
    <div className="w-full h-96 bg-white rounded-lg border overflow-hidden flex">
      {/* Main SVG area */}
      <div className="flex-1">
        <svg viewBox="0 0 1920 1080" className="w-full h-full" preserveAspectRatio="xMidYMid meet">
          {/* Background circles - rendered first (behind everything) */}
          {backgroundElements.map((circle) => {
            const Component = circle.component;
            return (
              <g key={circle.name} transform={circle.transform}>
                <Component fillColor="#6b7280" style={{ opacity: 0.8 }} />
              </g>
            );
          })}

          {/* Machine components - rendered after circles */}
          {machineComponents.map((machine) => {
            const Component = machine.component;

            return (
              <g key={machine.name} transform={machine.transform}>
                <Component fillColor={getMachineColor(machine.name)} style={{ opacity: 1 }} />
              </g>
            );
          })}

          {/* Dynamic Package icons - rendered last (on top) */}
          {packagePositions.slice(-10).map((position, index) => (
            <g
              key={`package-${position.packageId}-${index}`}
              transform={`translate(${position.x}, ${position.y})`}
            >
              <foreignObject x="-64" y="-64" width="128" height="128">
                <Package
                  size={128}
                  fill={position.color.fill}
                  stroke={position.color.stroke}
                  strokeWidth={2}
                  className="drop-shadow-md"
                />
              </foreignObject>
            </g>
          ))}
        </svg>
      </div>

      {/* Legend */}
      <div className="w-56 bg-gray-50 border-l p-4">
        <h3 className="text-lg font-semibold text-gray-700 mb-4">Packages</h3>
        {packagePositions.length === 0 ? (
          <p className="text-sm text-gray-500">No packages active</p>
        ) : (
          <div className="space-y-3">
            {packagePositions.slice(-5).map((position) => (
              <div key={`legend-${position.packageId}`} className="flex items-center space-x-3">
                <div className="flex-shrink-0">
                  <Package
                    size={24}
                    fill={position.color.fill}
                    stroke={position.color.stroke}
                    strokeWidth={2}
                  />
                </div>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    Package {position.packageId}
                  </p>
                  <p className="text-sm text-gray-500 truncate">{position.machineName}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
