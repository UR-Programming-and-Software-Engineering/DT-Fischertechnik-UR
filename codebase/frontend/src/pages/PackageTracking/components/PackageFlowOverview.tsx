import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Package, Clock, Boxes } from 'lucide-react';
import { usePackageStore } from '@/store/packageStore';
import { useSimulationStore } from '@/store/simulationStore';
import { PackageFlowOverviewProps } from '@/types/components';

function Progress({ value, className }: { value: number; className?: string }) {
  return (
    <div className={`w-full bg-gray-200 rounded ${className || ''}`}>
      <div className="bg-black h-full rounded" style={{ width: `${value}%`, height: '100%' }} />
    </div>
  );
}

export default function PackageFlowOverview({
  title = 'Package Flow Overview',
  description = 'Real-time tracking of packages through the production line',
  className = 'col-span-2 md:col-span-4',
  showMetrics = true,
}: PackageFlowOverviewProps) {
  const { packages, machineSteps } = usePackageStore();
  const { currentSimulationDetails } = useSimulationStore();

  // Calculate metrics
  const metrics = {
    activePackages: packages.length,
    avgProgress: 50, // This would need real calculation based on your requirements
  };

  // Calculate package progress
  const getPackageProgress = (pkg: any) => {
    const totalSteps = machineSteps.length + 1;
    const currentIndex = pkg.index;
    return Math.round(((currentIndex + 1) / totalSteps) * 100);
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
          </div>
          {showMetrics && (
            <div className="flex items-center space-x-6">
              <div className="text-center">
                <div className="flex items-center space-x-2">
                  <Package className="h-5 w-5 text-blue-600" />
                  <span className="text-2xl font-bold text-gray-900">{metrics.activePackages}</span>
                </div>
                <p className="text-sm text-gray-600">Processed Packages</p>
              </div>
              <div className="text-center">
                <div className="flex items-center space-x-2">
                  <Boxes className="h-5 w-5 text-orange-600" />
                  <span className="text-2xl font-bold text-gray-900">
                    {currentSimulationDetails?.expectedPackages || 0}
                  </span>
                </div>
                <p className="text-sm text-gray-600">Expected Packages</p>
              </div>
            </div>
          )}
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-6">
          {packages
            .slice()
            .reverse()
            .map((pkg, index) => (
              <div
                key={pkg.id}
                className="space-y-2 animate-in slide-in-from-top-2 fade-in duration-500"
                style={{ animationDelay: `${index * 100}ms` }}
              >
                <div className="flex justify-between items-center">
                  <span className="font-medium">{pkg.id}</span>
                  <span className="text-sm text-gray-600">Current: {pkg.machineName}</span>
                </div>
                <Progress value={getPackageProgress(pkg)} className="h-3" />
                <div className="flex justify-between text-xs text-gray-500">
                  <span></span>
                  <span>{getPackageProgress(pkg)}% Complete</span>
                </div>
              </div>
            ))}
        </div>
      </CardContent>
    </Card>
  );
}
