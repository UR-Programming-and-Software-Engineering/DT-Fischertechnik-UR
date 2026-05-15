import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { BarChart3, TrendingUp, Package, Target } from 'lucide-react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  LineElement,
  PointElement,
} from 'chart.js';
import { Bar, Line } from 'react-chartjs-2';

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend
);

// Define chart component props directly
interface SimulationChartsProps {
  overheatingCounter: Record<string, number>;
  hourlyPackages: number[];
  packageThroughput: {
    estimated: number;
    real: number;
  };
  timePerPackage: {
    estimated: number;
    real: number;
  };
  simulationId?: string;
  // Optional to avoid breaking existing code
}

export default function SimulationCharts({
  overheatingCounter,
  hourlyPackages,
  packageThroughput,
  timePerPackage,
}: SimulationChartsProps) {
  const formatMachineName = (key: string): string => {
    const nameMap: Record<string, string> = {
      SortingLine01: 'Sorting Line 01',
      ConveyorBelt01: 'Conveyor Belt 01',
      VacuumGripper01: 'Vacuum Gripper 01',
      VacuumGripper02: 'Vacuum Gripper 02',
      MultiProcessing01: 'Multi Processing 01',
      HighBay01: 'High Bay 01',
    };
    return nameMap[key] || key;
  };

  // Generate hour labels based on simulation duration
  const generateHourLabels = (hourlyData: number[]) => {
    return hourlyData.map((_, index) => `${index + 1}h`);
  };

  // Common chart options
  const commonTooltipOptions = {
    backgroundColor: 'rgba(79, 70, 229, 0.9)',
    titleColor: 'white',
    bodyColor: 'white',
    borderColor: 'rgba(79, 70, 229, 1)',
    borderWidth: 1,
    cornerRadius: 4,
  };

  const commonAxisOptions = {
    ticks: {
      color: 'rgba(107, 114, 128, 0.8)',
    },
    border: {
      display: false,
    },
  };

  return (
    <>
      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Machine Overheating Events Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="w-5 h-5" />
              Machine Overheating Counter
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <Bar
                data={{
                  labels: Object.keys(overheatingCounter || {}).map(formatMachineName),
                  datasets: [
                    {
                      label: 'Overheating Count',
                      data: Object.values(overheatingCounter || {}),
                      backgroundColor: 'rgba(79, 70, 229, 0.8)',
                      borderColor: 'rgba(79, 70, 229, 1)',
                      borderWidth: 1,
                      borderRadius: 2,
                    },
                  ],
                }}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      display: false,
                    },
                    title: {
                      display: false,
                    },
                    tooltip: commonTooltipOptions,
                  },
                  scales: {
                    y: {
                      beginAtZero: true,
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        stepSize: 1,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        color: 'rgba(229, 231, 235, 0.5)',
                      },
                      title: {
                        display: false,
                      },
                    },
                    x: {
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        maxRotation: 45,
                        minRotation: 45,
                        font: {
                          size: 10,
                        },
                      },
                      grid: {
                        display: false,
                      },
                      title: {
                        display: false,
                      },
                    },
                  },
                }}
              />
            </div>
          </CardContent>
        </Card>

        {/* Package Flow Over Time Line Chart */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="w-5 h-5" />
              Package Flow Over Time
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-64">
              <Line
                data={{
                  labels: generateHourLabels(hourlyPackages || []),
                  datasets: [
                    {
                      label: 'Packages per Hour',
                      data: hourlyPackages || [],
                      borderColor: 'rgba(79, 70, 229, 1)',
                      backgroundColor: 'rgba(79, 70, 229, 0.1)',
                      borderWidth: 2,
                      pointBackgroundColor: 'rgba(79, 70, 229, 1)',
                      pointBorderColor: 'white',
                      pointBorderWidth: 2,
                      pointRadius: 4,
                      pointHoverRadius: 6,
                      fill: true,
                      tension: 0.4,
                    },
                  ],
                }}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      display: false,
                    },
                    title: {
                      display: false,
                    },
                    tooltip: commonTooltipOptions,
                  },
                  scales: {
                    y: {
                      beginAtZero: true,
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        color: 'rgba(229, 231, 235, 0.5)',
                      },
                      title: {
                        display: true,
                        text: 'Packages',
                        color: 'rgba(107, 114, 128, 0.8)',
                        font: {
                          size: 11,
                        },
                      },
                    },
                    x: {
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 10,
                        },
                      },
                      grid: {
                        display: false,
                      },
                      title: {
                        display: true,
                        text: 'Time (Hours)',
                        color: 'rgba(107, 114, 128, 0.8)',
                        font: {
                          size: 11,
                        },
                      },
                    },
                  },
                }}
              />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Performance Comparison Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Package Throughput Comparison */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Package className="w-5 h-5" />
              Package Throughput
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-48">
              <Bar
                data={{
                  labels: ['Estimated', 'Real'],
                  datasets: [
                    {
                      label: 'Total Packages',
                      data: [packageThroughput?.estimated || 0, packageThroughput?.real || 0],
                      backgroundColor: [
                        'rgba(156, 163, 175, 0.8)', // Gray for estimated
                        'rgba(79, 70, 229, 0.8)', // Indigo for real
                      ],
                      borderColor: ['rgba(156, 163, 175, 1)', 'rgba(79, 70, 229, 1)'],
                      borderWidth: 1,
                      borderRadius: 2,
                    },
                  ],
                }}
                options={{
                  indexAxis: 'y' as const,
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      display: false,
                    },
                    title: {
                      display: false,
                    },
                    tooltip: commonTooltipOptions,
                  },
                  scales: {
                    x: {
                      beginAtZero: true,
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        color: 'rgba(229, 231, 235, 0.5)',
                      },
                      title: {
                        display: true,
                        text: 'Total Packages',
                        color: 'rgba(107, 114, 128, 0.8)',
                        font: {
                          size: 11,
                        },
                      },
                    },
                    y: {
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        display: false,
                      },
                    },
                  },
                }}
              />
            </div>
          </CardContent>
        </Card>

        {/* Time per Package Comparison */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Target className="w-5 h-5" />
              Time per Package
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="h-48">
              <Bar
                data={{
                  labels: ['Estimated', 'Real'],
                  datasets: [
                    {
                      label: 'Minutes per Package',
                      data: [timePerPackage?.estimated || 0, timePerPackage?.real || 0],
                      backgroundColor: [
                        'rgba(156, 163, 175, 0.8)', // Gray for estimated
                        'rgba(79, 70, 229, 0.8)', // Indigo for real
                      ],
                      borderColor: ['rgba(156, 163, 175, 1)', 'rgba(79, 70, 229, 1)'],
                      borderWidth: 1,
                      borderRadius: 2,
                    },
                  ],
                }}
                options={{
                  indexAxis: 'y' as const,
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: {
                      display: false,
                    },
                    title: {
                      display: false,
                    },
                    tooltip: commonTooltipOptions,
                  },
                  scales: {
                    x: {
                      beginAtZero: true,
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        color: 'rgba(229, 231, 235, 0.5)',
                      },
                      title: {
                        display: true,
                        text: 'Minutes per Package',
                        color: 'rgba(107, 114, 128, 0.8)',
                        font: {
                          size: 11,
                        },
                      },
                    },
                    y: {
                      ...commonAxisOptions,
                      ticks: {
                        ...commonAxisOptions.ticks,
                        font: {
                          size: 11,
                        },
                      },
                      grid: {
                        display: false,
                      },
                    },
                  },
                }}
              />
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
