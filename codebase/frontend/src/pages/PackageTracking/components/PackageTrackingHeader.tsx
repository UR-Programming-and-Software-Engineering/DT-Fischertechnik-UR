import React from 'react';
import { PackageTrackingInformation } from '@/types';
import { Card, CardContent } from '@/components/ui/card';
import { Clock, Activity, AlertTriangle, PieChart } from 'lucide-react';

export default function PackageTrackingHeader(packageTrackingData: PackageTrackingInformation) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      <Card>
        <CardContent className="pb-1">
          <div className="flex flex-col">
            <div className="flex items-center space-x-2 mb-2">
              <PieChart className="h-5 w-5 text-blue-600" />
              <p className="text-sm font-medium text-gray-600">Package Type</p>
            </div>

            <div className="mt-1 grid grid-cols-2 gap-2">
              <div className="flex items-center">
                <div className="h-3 w-3 rounded-full bg-red-500 mr-2"></div>
                <span className="text-sm text-gray-600">
                  Red: <span className="font-bold">{packageTrackingData.packagesByType.RED}</span>
                </span>
              </div>
              <div className="flex items-center">
                <div className="h-3 w-3 rounded-full bg-gray-200 mr-2"></div>
                <span className="text-sm text-gray-600">
                  White:{' '}
                  <span className="font-bold">{packageTrackingData.packagesByType.WHITE}</span>
                </span>
              </div>
              <div className="flex items-center">
                <div className="h-3 w-3 rounded-full bg-blue-500 mr-2"></div>
                <span className="text-sm text-gray-600">
                  Blue: <span className="font-bold">{packageTrackingData.packagesByType.BLUE}</span>
                </span>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-4">
          <div className="flex items-center space-x-2">
            <Clock className="h-5 w-5 text-green-600" />
            <div>
              <p className="text-sm font-medium text-gray-600">Average Time per Package</p>
              <p className="text-2xl font-bold text-gray-900">
                {Math.round(packageTrackingData.averageTimePerPackage / 6) / 10}m
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="p-4">
          <div className="flex items-center space-x-2">
            <Activity className="h-5 w-5 text-purple-600" />
            <div>
              <p className="text-sm font-medium text-gray-600">Throughput/Hour</p>
              <p className="text-2xl font-bold text-gray-900">
                {Math.round(packageTrackingData.troughputPerHour * 10) / 10} packages
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
