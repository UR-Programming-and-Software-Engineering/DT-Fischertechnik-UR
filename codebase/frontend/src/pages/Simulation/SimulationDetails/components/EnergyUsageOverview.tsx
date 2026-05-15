import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useEffect, useState, useRef, useMemo } from 'react';
import { Chart, registerables } from 'chart.js';
import { SimulationParameters } from '@/types/simulationControl';
Chart.register(...registerables);

// only used for the mock data if no props are provided
interface EnergyData {
  timestamp: string;
  totalEnergy: number;
}
export interface EnergyUsageOverviewProps {
  energyByMachine?: Record<string, number>;
  totalEnergyPerHour?: Record<number, number>;
  totalEnergyPerMinute?: Record<number, number>;
  parameters: SimulationParameters;
}

const BAR_COLORS = [
  '#10b981',
  '#3b82f6',
  '#f59e0b',
  '#ef4444',
  '#8b5cf6',
  '#14b8a6',
  '#f97316',
  '#d946ef',
  '#22c55e',
  '#06b6d4',
];

export default function EnergyUsageOverview({
  energyByMachine,
  totalEnergyPerHour,
  totalEnergyPerMinute,
  parameters,
}: EnergyUsageOverviewProps) {
  const [timelineData, setTimelineData] = useState<EnergyData[]>([]);

  // Calculate total duration in hours
  const totalHours = useMemo(() => {
    return (
      parameters.simulationDays * 24 +
      parameters.simulationHours +
      parameters.simulationMinutes / 60
    );
  }, [parameters]);

  // Process timeline data based on simulation duration
  useEffect(() => {
    if (!totalEnergyPerHour || Object.keys(totalEnergyPerHour).length === 0) return;

    let processedData: EnergyData[] = [];

    // Case 1: Duration > 10 hours - use hourly bins with ~10 evenly spaced labels
    if (totalHours > 10) {
      const hourKeys = Object.keys(totalEnergyPerHour)
        .map(Number)
        .sort((a, b) => a - b);
      processedData = hourKeys.map((hour) => ({
        timestamp: `${Math.floor(hour)}h`,
        totalEnergy: totalEnergyPerHour[hour] || 0,
      }));
    }
    // Case 2: Duration between 5-10 hours - use hourly bins with partial last bin
    else if (totalHours >= 5) {
      // Generate hourly bins
      const hourKeys = Object.keys(totalEnergyPerHour)
        .map(Number)
        .sort((a, b) => a - b);
      processedData = hourKeys.map((hour) => {
        // For whole hours
        if (Number.isInteger(hour)) {
          return {
            timestamp: `${hour}h`,
            totalEnergy: totalEnergyPerHour[hour] || 0,
          };
        }
        // For partial hours (like 4.5h)
        else {
          const hourPart = Math.floor(hour);
          const minutePart = Math.round((hour - hourPart) * 60);
          return {
            timestamp: `${hourPart}h ${minutePart}m`,
            totalEnergy: totalEnergyPerHour[hour] || 0,
          };
        }
      });
    }
    // Case 3: Duration < 5 hours - use minute-based bins with "Xh Ym" formatted labels
    else {
      // Use totalEnergyPerMinute data for more granular visualization
      if (totalEnergyPerMinute && Object.keys(totalEnergyPerMinute).length > 0) {
        // Generate minute-based bins from totalEnergyPerMinute
        const minuteKeys = Object.keys(totalEnergyPerMinute)
          .map(Number)
          .sort((a, b) => a - b);

        processedData = minuteKeys.map((minute) => {
          // Convert minute index to hours and minutes for display
          const hours = Math.floor(minute / 60);
          const mins = minute % 60;
          const timestamp = mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;

          return {
            timestamp,
            totalEnergy: totalEnergyPerMinute[minute] || 0,
          };
        });
      } else {
        // Fallback to hourly data if minute data is not available
        const hourKeys = Object.keys(totalEnergyPerHour)
          .map(Number)
          .sort((a, b) => a - b);
        processedData = hourKeys.map((hour) => {
          const hourPart = Math.floor(hour);
          const minutePart = Math.round((hour - hourPart) * 60);
          return {
            timestamp: minutePart > 0 ? `${hourPart}h ${minutePart}m` : `${hourPart}h`,
            totalEnergy: totalEnergyPerHour[hour] || 0,
          };
        });
      }
    }

    setTimelineData(processedData);
  }, [totalEnergyPerHour, totalEnergyPerMinute, totalHours]);

  // Derived energy-by-machine array from store
  const energyItems = useMemo(() => {
    const map = energyByMachine ?? {};
    const labelMap: Record<string, string> = {
      ConveyorBelt01: 'ConveyorBelt01',
      SortingLine01: 'SortingLine01',
      VacuumGripper01: 'VacuumGripper01',
      VacuumGripper02: 'VacuumGripper02',
      MultiProcessing01: 'MultiProcessing01',
      HighBay01: 'HighBay01',
    };
    const arr = Object.entries(map).map(([machine, kwh]) => ({
      label: labelMap[machine] ?? machine,
      kwh: Number(kwh ?? 0),
    }));
    // Sort descending for nicer display
    arr.sort((a, b) => b.kwh - a.kwh);
    return arr;
  }, [energyByMachine]);

  // Create refs for the chart canvases
  const timelineChartRef = useRef<HTMLCanvasElement>(null);
  const machineChartRef = useRef<HTMLCanvasElement>(null);

  // Chart instances refs
  const timelineChartInstance = useRef<Chart | null>(null);
  const machineChartInstance = useRef<Chart | null>(null);

  // Determine when backend simulation data is available for the active id
  const dataReady = Boolean(
    energyByMachine &&
      Object.keys(energyByMachine).length > 0 &&
      // For longer simulations, we need hourly data
      ((totalHours >= 5 && totalEnergyPerHour && Object.keys(totalEnergyPerHour).length > 0) ||
        // For shorter simulations, we can use either minute data or fall back to hourly data
        (totalHours < 5 &&
          ((totalEnergyPerMinute && Object.keys(totalEnergyPerMinute).length > 0) ||
            (totalEnergyPerHour && Object.keys(totalEnergyPerHour).length > 0))))
  );

  // Render charts once when the backend data becomes available (data is static)
  useEffect(() => {
    if (!dataReady || timelineData.length === 0) return; // wait until data is available

    // Left chart (timeline)
    if (timelineChartRef.current) {
      if (timelineChartInstance.current) {
        timelineChartInstance.current.destroy();
        timelineChartInstance.current = null;
      }
      const ctx = timelineChartRef.current.getContext('2d');
      if (ctx) {
        // For longer durations, we might want to reduce the number of x-axis labels to avoid clutter
        const labelsToShow = timelineData.map((item) => item.timestamp);
        const isShortSimulation = totalHours < 5;

        timelineChartInstance.current = new Chart(ctx, {
          type: 'line',
          data: {
            labels: labelsToShow,
            datasets: [
              {
                label: 'Energy Usage (Wh)',
                data: timelineData.map((item) => item.totalEnergy),
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37, 99, 235, 0.1)',
                borderWidth: 2,
                pointRadius: isShortSimulation ? 0 : 3,
                pointHoverRadius: isShortSimulation ? 4 : 5,
                tension: 0.2,
                fill: true,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { display: true, position: 'top' },
              tooltip: {
                mode: isShortSimulation ? 'index' : 'point',
                intersect: !isShortSimulation,
                callbacks: {
                  label: function (context) {
                    return `Consumption: ${context.parsed.y.toFixed(1)} Wh`;
                  },
                },
              },
            },
            scales: {
              x: {
                ticks: {
                  maxRotation: 45,
                  minRotation: 45,
                  callback: function (val, index) {
                    const step =
                      labelsToShow.length > 10
                        ? Math.max(1, Math.floor(labelsToShow.length / 10))
                        : 1;
                    return index % step === 0 ? this.getLabelForValue(val as number) : '';
                  },
                },
              },
              y: {
                title: { display: true, text: 'Energy Consumption (Wh)' },
                beginAtZero: false,
              },
            },
          },
        });
      }
    }

    if (machineChartRef.current) {
      if (machineChartInstance.current) {
        machineChartInstance.current.destroy();
        machineChartInstance.current = null;
      }

      const ctx = machineChartRef.current.getContext('2d');
      if (ctx) {
        const labels = energyItems.map((i) => i.label);
        const values = energyItems.map((i) => i.kwh);
        const barColors = labels.map((_, idx) => BAR_COLORS[idx % BAR_COLORS.length]);

        machineChartInstance.current = new Chart(ctx, {
          type: 'bar',
          data: {
            labels,
            datasets: [
              {
                label: 'Energy Usage (Wh)',
                data: values,
                backgroundColor: barColors,
                borderWidth: 1,
              },
            ],
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            indexAxis: 'y',
            plugins: {
              legend: { display: true, position: 'top' },
              tooltip: {
                callbacks: {
                  label: function (context) {
                    return `Energy: ${context.parsed.x.toFixed(1)} Wh`;
                  },
                },
              },
            },
            scales: {
              x: { title: { display: true, text: 'Energy (Wh)' }, beginAtZero: true },
              y: { ticks: { autoSkip: false } },
            },
          },
        });
      }
    }

    // Cleanup once on unmount
    return () => {
      if (timelineChartInstance.current) {
        timelineChartInstance.current.destroy();
        timelineChartInstance.current = null;
      }
      if (machineChartInstance.current) {
        machineChartInstance.current.destroy();
        machineChartInstance.current = null;
      }
    };
  }, [dataReady, timelineData]);

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {/* Energy Usage Timeline */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-lg">Energy Consumption Timeline</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-80">
            <canvas ref={timelineChartRef}></canvas>
          </div>
        </CardContent>
      </Card>

      {/* Energy Usage by Machine */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-lg">Energy Usage by Machine</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-80">{<canvas ref={machineChartRef}></canvas>}</div>
        </CardContent>
      </Card>
    </div>
  );
}
