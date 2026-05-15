'use client';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { useSimulationStore } from '@/store/simulationStore';
import { useNavigate } from 'react-router-dom';
import { formatDurationFromMs } from '@/lib/simulation-utils';
import { useState, useEffect } from 'react';
import { AlertTriangle, CheckCircle, Activity, X, ExternalLink } from 'lucide-react';

export function SimulationStatus() {
  const {
    simulationRunning,
    currentSimulationDetails,
    simulationError,
    fetchCurrentSimulation,
    clearCurrentSimulation,
  } = useSimulationStore();

  const navigate = useNavigate();
  const [isExiting, setIsExiting] = useState(false);
  const [hasBeenVisible, setHasBeenVisible] = useState(false);

  // Check if component should be visible
  const shouldBeVisible = simulationRunning || currentSimulationDetails || simulationError;

  // Track if component has ever been visible
  useEffect(() => {
    if (shouldBeVisible && !hasBeenVisible) {
      setHasBeenVisible(true);
    }
  }, [shouldBeVisible, hasBeenVisible]);

  // Handle exit animation
  useEffect(() => {
    if (!shouldBeVisible && hasBeenVisible && !isExiting) {
      setIsExiting(true);
      // Remove component after animation completes
      setTimeout(() => {
        setIsExiting(false);
        setHasBeenVisible(false); // Reset for next time
      }, 300);
    }
  }, [shouldBeVisible, hasBeenVisible, isExiting]);

  // Don't render if not visible and not exiting, or if never been visible
  if ((!shouldBeVisible && !isExiting) || !hasBeenVisible) {
    return null;
  }

  // Calculate derived values when we have simulation data
  const getProgressPercentage = (): number => {
    if (!currentSimulationDetails) return 0;

    const elapsed = getElapsedTime();

    const totalDuration = Number(currentSimulationDetails.duration) / 60000; // convert ms to minutes
    const progress = Math.min((elapsed / 60000 / totalDuration) * 100, 100);

    return progress;
  };
  const getElapsedTime = (): number => {
    if (!currentSimulationDetails) return 0;

    const currentTime = currentSimulationDetails.timeEnded
      ? new Date(currentSimulationDetails.timeEnded).getTime()
      : new Date().getTime();

    // For running simulations, calculate based on elapsed time
    const created = new Date(currentSimulationDetails.timeCreated).getTime();
    const elapsed = currentTime - created; // elapsed in ms
    const pseudoElapsed =
      Math.abs(elapsed - currentSimulationDetails.duration) < 300
        ? currentSimulationDetails.duration
        : elapsed;
    return pseudoElapsed;
  };

  const formatElapsedTime = (): string => {
    if (!currentSimulationDetails) return '0m';

    const elapsed = Math.floor(getElapsedTime()); // elapsed time in ms

    return formatDurationFromMs(elapsed);
  };

  const isFinished = (): boolean => {
    // Simulation is finished if timeEnded is set or if not running and we have simulation details
    return (
      !!currentSimulationDetails?.timeEnded || (!simulationRunning && !!currentSimulationDetails)
    );
  };

  const handleClear = () => {
    clearCurrentSimulation();
    // shouldBeVisible wird false und triggert die Exit-Animation im useEffect
  };

  const handleSeeInOverview = () => {
    if (currentSimulationDetails?.id) {
      navigate(`/simulation-analysis/${currentSimulationDetails.id}`);
    }
  };

  return (
    <Card
      className={`border-blue-200/60 bg-gradient-to-br from-blue-50/80 via-indigo-50/60 to-purple-50/40 shadow-sm
        ${
          isExiting
            ? 'animate-out slide-out-to-top-4 fade-out duration-300'
            : 'animate-in slide-in-from-top-4 fade-in duration-300'
        }`}
    >
      <CardHeader className="pb-4">
        <CardTitle className="flex items-center justify-between text-base">
          <div className="flex items-center space-x-2.5">
            <div className="p-1.5 rounded-lg bg-blue-100 text-blue-700">
              <Activity className="h-4 w-4" />
            </div>
            <span className="font-semibold text-gray-800">Simulation Status</span>
          </div>
          {isFinished() && (
            <div className="flex items-center space-x-2">
              <Badge
                variant="default"
                className="bg-green-500 hover:bg-green-600 text-white shadow-sm px-2.5 py-1"
              >
                <CheckCircle className="h-3 w-3 mr-1.5" />
                Complete
              </Badge>
              <Button
                size="sm"
                variant="outline"
                onClick={handleSeeInOverview}
                className="text-blue-700 border-blue-200 hover:bg-blue-50 h-8 px-3 text-xs font-medium shadow-sm"
              >
                <ExternalLink className="h-3 w-3 mr-1.5" />
                View Details
              </Button>
              <Button
                size="sm"
                variant="ghost"
                onClick={handleClear}
                className="text-gray-500 hover:text-gray-700 hover:bg-gray-100 h-8 w-8 p-0"
              >
                <X className="h-3.5 w-3.5" />
              </Button>
            </div>
          )}
        </CardTitle>
      </CardHeader>

      <CardContent className="space-y-4 pt-0">
        {/* Error Alert */}
        {simulationError && (
          <div className="relative overflow-hidden bg-red-50 border border-red-200 rounded-xl p-3">
            <div className="absolute top-0 left-0 w-1 h-full bg-red-500"></div>
            <div className="flex items-center space-x-2 ml-2">
              <div className="p-1 rounded-full bg-red-100">
                <AlertTriangle className="h-3.5 w-3.5 text-red-600" />
              </div>
              <span className="text-sm font-medium text-red-800">{simulationError}</span>
            </div>
          </div>
        )}

        {/* Simulation Details */}
        {currentSimulationDetails ? (
          <>
            {/* Header Info with elegant typography */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-6">
                  <div>
                    <div className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-0.5">
                      Simulation ID
                    </div>
                    <div className="text-sm font-mono font-semibold text-gray-800 bg-gray-100 px-2 py-1 rounded-md inline-block">
                      {currentSimulationDetails.id}
                    </div>
                  </div>
                  <div className="h-8 w-px bg-gradient-to-b from-transparent via-gray-300 to-transparent"></div>
                  <div>
                    <div className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-0.5">
                      Name
                    </div>
                    <div className="text-sm font-semibold text-gray-800">
                      {currentSimulationDetails.name}
                    </div>
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-0.5">
                    Started
                  </div>
                  <div className="text-sm text-gray-700">
                    {new Date(currentSimulationDetails.timeCreated).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                    })}{' '}
                    at{' '}
                    {new Date(currentSimulationDetails.timeCreated).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </div>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <div className="text-sm font-semibold text-gray-700">Progress</div>
                  <div className="flex items-center space-x-2">
                    <div className="text-lg font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                      {getProgressPercentage().toFixed(1)}%
                    </div>
                  </div>
                </div>

                <div className="relative">
                  <div className="w-full bg-gray-200/70 rounded-full h-2.5 overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-blue-500 via-blue-600 to-indigo-600 rounded-full transition-all duration-500 ease-out shadow-sm"
                      style={{ width: `${getProgressPercentage()}%` }}
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center space-x-1">
                    <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                    <span className="text-gray-600 font-medium">
                      Elapsed:{' '}
                      <span className="text-gray-800 font-semibold">{formatElapsedTime()}</span>
                    </span>
                  </div>
                  <span className="text-gray-600">
                    Duration:{' '}
                    <span className="text-gray-800 font-semibold">
                      {formatDurationFromMs(currentSimulationDetails.duration)}
                    </span>
                  </span>
                </div>
              </div>

              {/* Status and Metrics with refined spacing */}
              <div className="flex items-center justify-between pt-2 border-t border-gray-200/60">
                {/* Status Badges */}
                <div className="flex items-center gap-2">
                  {simulationRunning && !currentSimulationDetails.timeEnded ? (
                    <Badge
                      variant="secondary"
                      className="flex items-center space-x-1.5 bg-blue-100 text-blue-700 border-blue-200 px-2.5 py-1"
                    >
                      <div className="w-2 h-2 bg-blue-500 rounded-full animate-pulse"></div>
                      <span className="font-medium">Running</span>
                    </Badge>
                  ) : (
                    <Badge
                      variant="default"
                      className="flex items-center space-x-1.5 bg-green-100 text-green-700 border-green-200 px-2.5 py-1"
                    >
                      <CheckCircle className="h-3 w-3" />
                      <span className="font-medium">Complete</span>
                    </Badge>
                  )}

                  {currentSimulationDetails.overheatingCounter &&
                    Object.values(currentSimulationDetails.overheatingCounter).some(
                      (count) => count > 0
                    ) && (
                      <Badge
                        variant="destructive"
                        className="flex items-center space-x-1.5 bg-red-100 text-red-700 border-red-200 px-2.5 py-1"
                      >
                        <AlertTriangle className="h-3 w-3" />
                        <span className="font-medium">
                          {(() => {
                            const totalOverheatingEvents = Object.values(
                              currentSimulationDetails.overheatingCounter
                            ).reduce((sum, count) => sum + count, 0);
                            return `${totalOverheatingEvents} ${
                              totalOverheatingEvents === 1
                                ? 'Overheating Event'
                                : 'Overheating Events'
                            }`;
                          })()}
                        </span>
                      </Badge>
                    )}
                </div>

                {/* Package Information with better visual hierarchy */}
                <div className="flex items-center space-x-4 text-sm">
                  <div className="text-center">
                    <div className="text-xs text-gray-500 font-medium">Processed</div>
                    <div className="text-lg font-bold text-gray-800">
                      {currentSimulationDetails.totalPackages}
                    </div>
                  </div>
                  <div className="h-8 w-px bg-gradient-to-b from-transparent via-gray-300 to-transparent"></div>
                  <div className="text-center">
                    <div className="text-xs text-gray-500 font-medium">Expected</div>
                    <div className="text-lg font-bold text-gray-600">
                      {currentSimulationDetails.expectedPackages}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </>
        ) : simulationRunning && !simulationError ? (
          /* Loading State */
          <div className="flex items-center justify-center py-6">
            <div className="flex items-center space-x-3">
              <div className="relative">
                <div className="w-8 h-8 border-2 border-blue-200 rounded-full"></div>
                <div className="absolute top-0 left-0 w-8 h-8 border-2 border-blue-600 rounded-full border-t-transparent animate-spin"></div>
              </div>
              <div>
                <div className="text-sm font-medium text-gray-700">Starting simulation...</div>
                <div className="text-xs text-gray-500">Please wait while we initialize</div>
              </div>
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}
