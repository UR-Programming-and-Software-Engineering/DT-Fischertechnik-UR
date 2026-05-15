import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { useEffect, useMemo, useState, useRef } from 'react';
import { Activity, Hash, TrendingUp, Clock, Target } from 'lucide-react';
import { CARD_HEIGHT } from '@/constants/layout';
import { CARD_WIDTH } from '@/constants/layout';
import { MachineState } from '@/types/machines';
import { ClickableCardProps } from '@/types/components';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from 'chart.js';
import type { ChartOptions, ChartData } from 'chart.js';
import { useMachineStore } from '@/store/machineStore';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend);

function ClickableCard(props: ClickableCardProps) {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [activity, setActivity] = useState<number[]>(() => Array(30).fill(0));
  const stateRef = useRef<MachineState>(props.state);
  // Track latest state in a ref to avoid stale closures inside setInterval
  useEffect(() => {
    stateRef.current = props.state;
  }, [props.state]);

  // Sample with dynamic interval based on simulation speed
  useEffect(() => {
    const baseInterval = 1000; // 1 second base
    const speedMultiplier = props.speedMultiplier;

    // Calculate adjusted interval: BaseSpeed * (1 + sqrt(SpeedMultiplier))
    const adjustedInterval =
      speedMultiplier >= 1 ? baseInterval / (1 + Math.log(speedMultiplier)) : baseInterval;

    const id = setInterval(() => {
      const value = stateRef.current === MachineState.RUNNING ? 1 : 0;
      setActivity((prev) => {
        const next = prev.length >= 30 ? prev.slice(1) : prev.slice();
        next.push(value);
        return next;
      });
    }, adjustedInterval);

    return () => clearInterval(id);
  }, [props.speedMultiplier]);

  // State-based styles and labels
  const getStatusInfo = (state: MachineState) => {
    switch (state) {
      case MachineState.RUNNING:
        return {
          label: 'Running',
          bgColor: 'bg-green-100',
          textColor: 'text-green-700',
          dotColor: 'bg-green-500',
        };
      case MachineState.IDLE:
        return {
          label: 'Idle',
          bgColor: 'bg-orange-100',
          textColor: 'text-orange-700',
          dotColor: 'bg-orange-500',
        };
      case MachineState.OVERHEATED:
        return {
          label: 'Overheated',
          bgColor: 'bg-red-100',
          textColor: 'text-red-700',
          dotColor: 'bg-red-500',
        };
      default:
        return {
          label: 'Unknown',
          bgColor: 'bg-gray-100',
          textColor: 'text-gray-700',
          dotColor: 'bg-gray-500',
        };
    }
  };

  const statusInfo = getStatusInfo(props.state);

  const activityChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    animation: false,
    plugins: {
      legend: { display: false },
      tooltip: { enabled: false },
    },
    scales: {
      x: { display: false, grid: { display: false }, border: { display: false } },
      y: {
        min: 0,
        max: 1,
        display: false,
        ticks: { stepSize: 1, display: false },
        grid: { display: false },
        border: { display: false },
      },
    },
    elements: {
      line: { tension: 0.2 },
      point: { radius: 0 },
    },
  };

  // Build chart data from the activity array
  const activityChartData: ChartData<'line'> = useMemo(
    () => ({
      labels: Array(activity.length).fill(''),
      datasets: [
        {
          data: activity,
          borderColor: '#3b82f6', // blue-500
          backgroundColor: 'rgba(59,130,246,0.15)',
          fill: true,
          borderWidth: 4,
        },
      ],
    }),
    [activity]
  );

  const { machines } = useMachineStore();

  const machineData = machines[props.name];

  const [energyData, setEnergyData] = useState<number[]>([]);

  useEffect(() => {
    if (
      machineData?.energyKWHperMinute &&
      Array.isArray(machineData.energyKWHperMinute) &&
      machineData.energyKWHperMinute.length > 0
    ) {
      const newData = machineData.energyKWHperMinute
        .map((value: any) => (typeof value === 'number' ? value : parseFloat(String(value))))
        .slice(-60); // Keep last 60 points

      setEnergyData(newData);
    }
  }, [machineData?.energyKWHperMinute, props.name]);

  const energyChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    elements: {
      line: {
        tension: 0.2,
      },
    },
    scales: {
      x: {
        ticks: {
          maxRotation: 0,
          color: 'rgb(148 163 184)',
          autoSkip: true,
          maxTicksLimit: 6,
        },
        grid: {
          display: false,
        },
      },
      y: {
        beginAtZero: true,
        min: 0,
        max: 2,
        ticks: {
          color: 'rgb(148 163 184)',
          callback: (value) => `${value}W`,
          stepSize: 0.2, // Show ticks at intervals of 0.2
        },
        grid: {
          color: 'rgba(148, 163, 184, 0.1)',
        },
      },
    },
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        enabled: false,
      },
    },
  };
  const energyChartData: ChartData<'line'> = useMemo(() => {
    // Create time labels for the x-axis (minute indices)
    const timeLabels = energyData.map((_, index) => {
      // Calculate relative minute for each data point
      // If we have 60 points, the earliest is "minute -59" and the latest is "minute 0"
      const minuteOffset = -1 * (energyData.length - 1 - index);
      return minuteOffset === 0 ? 'now' : `${minuteOffset} min`;
    });

    return {
      labels: timeLabels,
      datasets: [
        {
          data: energyData,
          borderColor: '#3b82f6',
          borderWidth: 2,
          pointRadius: 0,
          fill: true,
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          tension: 0.25,
        },
      ],
    };
  }, [energyData]);

  const handleCardClick = () => {
    setIsModalOpen(true);
  };

  return (
    <>
      {/* Card */}
      <div
        className={`${CARD_WIDTH} ${
          props.cardHeight ?? CARD_HEIGHT
        } hover:bg-stone-100 border bg-stone-50 border-gray-200 rounded-lg p-6 flex flex-col items-center space-y-4 cursor-pointer transition-all duration-200 hover:shadow-md hover:scale-[1.02]`}
        onClick={handleCardClick}
        style={{ cursor: 'pointer' }}
      >
        <div className="w-full">
          <div className="flex flex-row items-center space-x-2">
            <h2 className="text-lg font-semibold text-gray-800">{props.name}</h2>
            <div
              className={`flex items-center space-x-2 ${statusInfo.bgColor} ${statusInfo.textColor} rounded-lg px-3 py-1`}
            >
              <div className={`w-2 h-2 ${statusInfo.dotColor} rounded-full`}></div>
              <span className="text-sm font-semibold">{statusInfo.label}</span>
            </div>
          </div>
        </div>

        {/* Machine-specific data */}
        <div className="w-full">{props.children}</div>
        {/* Current activity - line chart (live) */}
        <div className="w-full mt-6">
          <div className="text-xs font-medium text-gray-600 mb-1">Current activity</div>
          <div className="h-12 w-full border border-gray-200 rounded-md bg-white relative overflow-hidden px-1 pt-2 pb-2 flex items-center justify-center">
            <Line options={activityChartOptions} data={activityChartData} />
          </div>
        </div>
      </div>

      {/* shadcn Dialog Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="sm:max-w-[680px] max-w-[95vw] p-0 gap-0 border-0 shadow-2xl">
          {/* Custom Header mit Gradient */}
          <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-6 rounded-t-lg">
            <DialogHeader>
              <DialogTitle className="text-xl font-bold text-white flex items-center gap-2">
                <Activity className="w-5 h-5" />
                {props.name}
              </DialogTitle>
              <DialogDescription className="text-blue-100 mt-2">
                Detailed machine-specific information and current state
              </DialogDescription>
            </DialogHeader>
          </div>

          {/* Modal Body */}
          <div className="p-6 space-y-6 bg-white rounded-b-lg">
            {/* State + Machine Name (same row) */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Machine Name rectangle (left) */}
              <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
                <div className="flex items-center space-x-2 mb-2">
                  <Hash className="w-4 h-4 text-gray-500" />
                  <span className="text-sm font-medium text-gray-500">Machine Name</span>
                </div>
                <div className="text-lg font-semibold text-gray-900">{props.name}</div>
              </div>

              {/* State rectangle (right) */}
              <div className={`rounded-lg p-4 border border-gray-200 ${statusInfo.bgColor}`}>
                <div className="flex items-center space-x-2 mb-2">
                  <Activity className="w-4 h-4 text-gray-600" />
                  <span className="text-sm font-medium text-gray-600">Activity</span>
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className={`w-3 h-3 ${statusInfo.dotColor} rounded-full`}></span>
                    <span className={`font-semibold ${statusInfo.textColor}`}>
                      {statusInfo.label}
                    </span>
                  </div>
                  <Activity className="w-4 h-4 opacity-70" />
                </div>
              </div>
            </div>

            {/* Machine Data */}
            <div className="bg-gray-50 rounded-lg p-4">
              <h3 className="text-sm font-medium text-gray-600 mb-3">Machine Data</h3>
              {props.children}
            </div>

            {/* Key Performance Indicators (KPIs) */}
            <div className="bg-gradient-to-r from-slate-50 to-gray-50 rounded-lg p-4 border border-slate-200">
              <div className="text-sm font-medium text-slate-700 mb-4 flex items-center gap-2">
                <TrendingUp className="w-4 h-4" />
                Current Machine Status
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Overload */}
                <div className="bg-white rounded-lg p-3 border border-slate-200 shadow-sm">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center space-x-2">
                      <Clock className="w-4 h-4 text-blue-600" />
                      <span className="text-xs font-medium text-blue-700">Current overload</span>
                    </div>
                    <span className="text-xs font-semibold text-blue-700">
                      {props.currentOverload}%
                    </span>
                  </div>
                  <div className="w-full bg-blue-100 rounded-full h-2">
                    <div
                      className="bg-blue-600 h-2 rounded-full"
                      style={{ width: `${props.currentOverload}%` }}
                    ></div>
                  </div>
                </div>

                {/* Overheating Count */}
                <div className="bg-white rounded-lg p-3 border border-slate-200 shadow-sm">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <Target className="w-4 h-4 text-purple-600" />
                      <span className="text-xs font-medium text-purple-700">Overheating Count</span>
                    </div>
                    <div
                      className={`px-3 py-1 rounded-full text-sm font-semibold ${
                        props.overheatingCounter === 0
                          ? 'bg-gray-100 text-gray-700'
                          : 'bg-purple-100 text-purple-700'
                      }`}
                    >
                      {props.overheatingCounter}{' '}
                      {props.overheatingCounter === 1 ? 'event' : 'events'}
                    </div>
                  </div>
                </div>
              </div>
            </div>

            {/* Real-time energy usage chart */}
            <div className="bg-white rounded-lg p-4 border border-gray-200">
              <div className="text-sm font-medium text-gray-700 mb-2">
                Energy consumption over time (Wh/min)
              </div>
              <div className="h-20 w-full relative">
                <Line options={energyChartOptions} data={energyChartData} />
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
              <Button
                onClick={() => setIsModalOpen(false)}
                className="sm:w-auto w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white order-1 sm:order-2"
              >
                Close
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
export default ClickableCard;
