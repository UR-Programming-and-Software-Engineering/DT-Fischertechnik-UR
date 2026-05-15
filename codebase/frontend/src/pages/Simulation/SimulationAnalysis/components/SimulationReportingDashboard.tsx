import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { BarChart3, Calendar, Package, Clock, TrendingUp } from 'lucide-react';
import { SimulationDataDetails } from '@/types/simulation';
import { formatDurationFromMs } from '@/lib/simulation-utils';

interface SimulationReportingDashboardProps {
  simulations?: SimulationDataDetails[];
}

export default function SimulationReportingDashboard({
  simulations = [],
}: SimulationReportingDashboardProps) {
  const calculatePerformance = (real: number, expected: number) => {
    const percentage = ((real - expected) / expected) * 100;
    return {
      percentage: percentage.toFixed(1),
      isPositive: real >= expected,
    };
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <BarChart3 className="w-5 h-5" />
          Simulation Reporting Dashboard
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="border-b-0">
                <TableHead className="w-40 border-r border-gray-200"></TableHead>
                {simulations.map((simulation, index) => (
                  <TableHead
                    key={simulation.id}
                    className={`text-center p-0 ${
                      index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                    }`}
                  >
                    <div
                      className={`bg-indigo-50 border border-indigo-200 rounded-t-lg mt-2 p-4 ${
                        index < simulations.length - 1 ? 'mr-px' : ''
                      }`}
                    >
                      <div className="flex items-center justify-center gap-2">
                        <BarChart3 className="w-4 h-4 text-indigo-600" />
                        <span className="font-semibold text-indigo-700">{simulation.name}</span>
                      </div>
                    </div>
                  </TableHead>
                ))}
              </TableRow>
            </TableHeader>
            <TableBody>
              {/* Start Date Row */}
              <TableRow className="border-b border-gray-100">
                <TableCell className="font-medium bg-gray-50 border-r border-gray-200">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-green-500" />
                    Start Date
                  </div>
                </TableCell>
                {simulations.map((simulation, index) => (
                  <TableCell
                    key={`start-${simulation.id}`}
                    className={`text-center bg-white border-l-2 border-l-indigo-100 ${
                      index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                    }`}
                  >
                    <div className="py-2">
                      <span className="text-sm text-gray-700">
                        {new Date(simulation.timeCreated).toLocaleDateString('en-GB')}
                      </span>
                    </div>
                  </TableCell>
                ))}
              </TableRow>

              {/* Duration Row */}
              <TableRow className="border-b border-gray-100">
                <TableCell className="font-medium bg-gray-50 border-r border-gray-200">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-purple-500" />
                    Duration
                  </div>
                </TableCell>
                {simulations.map((simulation, index) => (
                  <TableCell
                    key={`duration-${simulation.id}`}
                    className={`text-center bg-white border-l-2 border-l-indigo-100 ${
                      index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                    }`}
                  >
                    <div className="py-2">
                      <span className="text-sm text-gray-700">
                        {formatDurationFromMs(simulation.duration)}
                      </span>
                    </div>
                  </TableCell>
                ))}
              </TableRow>

              {/* Minutes per Package Row */}
              <TableRow className="border-b border-gray-100">
                <TableCell className="font-medium bg-gray-50 border-r border-gray-200">
                  <div className="flex items-center gap-2">
                    <Clock className="w-4 h-4 text-blue-500" />
                    Packages per hour
                  </div>
                </TableCell>
                {simulations.map((simulation, index) => (
                  <TableCell
                    key={`minutes-${simulation.id}`}
                    className={`text-center bg-white border-l-2 border-l-indigo-100 ${
                      index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                    }`}
                  >
                    <div className="py-2">
                      <div className="flex flex-col items-center">
                        <span className="text-lg font-bold text-gray-900">
                          {(60 / Math.max(simulation.timePerPackage.real, 1)).toFixed(1)}
                        </span>
                        <span className="text-xs text-gray-500">packages</span>
                      </div>
                    </div>
                  </TableCell>
                ))}
              </TableRow>

              {/* Total Packages Row */}
              <TableRow className="border-b border-gray-100">
                <TableCell className="font-medium bg-gray-50 border-r border-gray-200">
                  <div className="flex items-center gap-2">
                    <Package className="w-4 h-4 text-orange-500" />
                    Total Energy
                  </div>
                </TableCell>
                {simulations.map((simulation, index) => (
                  <TableCell
                    key={`total-${simulation.id}`}
                    className={`text-center bg-white border-l-2 border-l-indigo-100 ${
                      index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                    }`}
                  >
                    <div className="py-2">
                      <div className="flex flex-col items-center">
                        <span className="text-lg font-bold text-indigo-600">
                          {simulation.totalEnergyUsage?.toFixed(1)}
                        </span>
                        <span className="text-xs text-gray-500">watt-hours</span>
                      </div>
                    </div>
                  </TableCell>
                ))}
              </TableRow>

              {/* Expected vs Real Packages Row */}
              <TableRow>
                <TableCell className="font-medium bg-gray-50 border-r border-gray-200">
                  <div className="flex items-center gap-2">
                    <TrendingUp className="w-4 h-4 text-purple-500" />
                    <div>
                      <div>Expected vs Real</div>
                      <div className="text-xs text-gray-500">Packages</div>
                    </div>
                  </div>
                </TableCell>
                {simulations.map((simulation, index) => {
                  const performance = calculatePerformance(
                    simulation.totalPackages,
                    simulation.expectedPackages
                  );
                  return (
                    <TableCell
                      key={`performance-${simulation.id}`}
                      className={`text-center bg-white border-l-2 border-l-indigo-100 ${
                        index < simulations.length - 1 ? 'border-r border-gray-200' : ''
                      }`}
                    >
                      <div className="py-2">
                        <div className="space-y-2">
                          {/* Expected vs Real comparison */}
                          {/* <div className="grid grid-cols-2 gap- text-xs"> */}
                          <div className="flex justify-center gap-8 text-xs">
                            <div>
                              <div className="text-gray-500 font-bold uppercase tracking-wide">
                                Expected
                              </div>
                              <div className="font-semibold text-gray-700">
                                {simulation.expectedPackages.toLocaleString()}
                              </div>
                            </div>
                            <div>
                              <div className="text-gray-500 font-bold uppercase tracking-wide">
                                Real
                              </div>
                              <div className="font-semibold text-gray-700">
                                {simulation.totalPackages.toLocaleString()}
                              </div>
                            </div>
                          </div>

                          {/* Performance indicator */}
                          <div className="flex justify-center">
                            <Badge
                              variant={performance.isPositive ? 'default' : 'destructive'}
                              className={`text-xs ${
                                performance.isPositive
                                  ? 'bg-green-500 hover:bg-green-600 text-white'
                                  : 'bg-red-500 hover:bg-red-600 text-white'
                              }`}
                            >
                              {performance.isPositive ? '+' : ''}
                              {performance.percentage}%
                            </Badge>
                          </div>
                        </div>
                      </div>
                    </TableCell>
                  );
                })}
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
}
