import ProductionLineFlow from '@/pages/PackageTracking/components/ProductionLineFlow';
import PackageFlowOverview from '@/pages/PackageTracking/components/PackageFlowOverview';
import { PackageTrackingInformation } from '@/types';
import PackageTrackingHeader from '@/pages/PackageTracking/components/PackageTrackingHeader';
import { usePackageTrackingPolling } from '@/hooks/usePackageTrackingPolling';

export default function PackageTracking() {
  const packageTrackingData: PackageTrackingInformation = usePackageTrackingPolling();

  return (
    <div className="bg-gray-100">
      <div className="container mx-auto p-6">
        <div className="space-y-6">
          <PackageTrackingHeader {...packageTrackingData} />
          <ProductionLineFlow />
          <PackageFlowOverview />
        </div>
      </div>
    </div>
  );
}
