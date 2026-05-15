import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AlertTriangle } from 'lucide-react';
import { OverheatingEventsProps } from '@/types/energy';
import { formatMachineName } from '@/lib/simulation-utils';

export default function OverheatingEvents({
  overheatingCounter,
  enrichedMachineData,
  analysisOverheatingEvents,
}: OverheatingEventsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg flex items-center gap-2">
          <AlertTriangle className="w-5 h-5 text-red-500" />
          Overheating Events & Issues
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {analysisOverheatingEvents.map((event, index) => (
            <div
              key={index}
              className="border-l-4 border-gray-400 pl-6 py-4 bg-gray-50 rounded-r-lg"
            >
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  {/* Main Type Header - Most Prominent */}
                  <div className="mb-3">
                    <h3 className="text-lg font-bold text-gray-700 mb-1">{event.type}</h3>
                    <div className="h-1 w-16 bg-gray-500 rounded"></div>
                  </div>

                  {/* Machine Details - Different content based on type */}
                  <div className="flex items-center gap-6">
                    <div className="flex items-center gap-3">
                      <span className="text-sm font-medium text-gray-600">Machine:</span>
                      <span className="text-base font-semibold text-gray-900">
                        {formatMachineName(event.machine)}
                      </span>
                    </div>

                    {event.type === 'Total Overheating Events' ? (
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-medium text-gray-600">
                          Overheating Events:
                        </span>
                        <Badge
                          variant="outline"
                          className="bg-gray-100 text-gray-700 border-gray-300 font-semibold"
                        >
                          {event.count}
                        </Badge>
                      </div>
                    ) : (
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-medium text-gray-600">
                          Energy-Per-Package:
                        </span>
                        <Badge
                          variant="outline"
                          className="bg-gray-100 text-gray-700 border-gray-300 font-semibold"
                        >
                          {
                            enrichedMachineData.find(
                            (machine) => machine.machine === event.machine
                            )?.energyPerPackage
                          }{' '}
                          Wh
                        </Badge>
                        <span className="text-sm font-medium text-gray-600">Overheating:</span>
                        <Badge
                          variant="outline"
                          className={`${
                            event.count > 0
                            ? 'bg-red-100 text-red-700 border-red-300'
                            : 'bg-green-100 text-green-700 border-green-300'
                          } font-semibold`}
                        >
                          {event.count}
                        </Badge>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}

          {/* Machine-specific overheating breakdown */}
          <div className="mt-6">
            <h3 className="text-lg font-bold text-gray-700 mb-3">
              Machine-Specific Overheating Analysis
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Object.entries(overheatingCounter || {}).map(([machine, count]) => (
                <div key={machine} className="p-3 rounded-lg border border-gray-200 bg-gray-50">
                  <div className="flex items-center justify-between">
                    <span className="font-medium">{formatMachineName(machine)}</span>
                    <Badge
                      variant="outline"
                      className={`${
                        count > 0
                          ? 'bg-red-100 text-red-700 border-red-300'
                          : 'bg-green-100 text-green-700 border-green-300'
                      } font-semibold`}
                    >
                      {count} overheating events
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
