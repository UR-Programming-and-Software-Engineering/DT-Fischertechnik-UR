import { Link, useLocation } from 'react-router-dom';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ChevronDown, Activity } from 'lucide-react';
import { useSimulationStore } from '@/store/simulationStore';

const Header = () => {
  // Aktuelle Route mit useLocation aus react-router
  const { pathname } = useLocation();

  // Simulation state from Zustand store
  const { simulationRunning } = useSimulationStore();

  return (
    <header className="bg-white text-gray-800 py-3 px-6 h-16 flex items-center border-b border-gray-200">
      <div className="container mx-auto flex items-center justify-between">
        {/* Logo with brand color and modern style - digital twin concept */}
        <div className="flex items-center gap-2">
          <svg
            width="48"
            height="32"
            viewBox="0 0 48 32"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            {/* Pill-shaped background */}
            <rect x="2" y="2" width="44" height="28" rx="14" fill="#4338ca" />
            {/* Two connected circles representing digital twin concept */}
            <circle cx="16" cy="16" r="6" stroke="white" strokeWidth="2" fill="none" />
            <circle cx="32" cy="16" r="6" stroke="white" strokeWidth="2" fill="none" />
            {/* Connection lines showing data flow */}
            <path d="M16 16 L32 16" stroke="white" strokeWidth="2" strokeLinecap="round" />
            {/* Small dots in circles */}
            <circle cx="16" cy="16" r="2" fill="white" />
            <circle cx="32" cy="16" r="2" fill="white" />
          </svg>
          <span
            className="text-2xl font-extrabold tracking-tight text-indigo-700 select-none"
            style={{ letterSpacing: '0.03em' }}
          >
            Digital Twin
          </span>
        </div>

        {/* Zentrale Navigation */}
        <div className="absolute left-1/2 transform -translate-x-1/2">
          <div className="flex bg-gray-100 rounded-full px-1 py-1">
            <Link to="/">
              <Button
                variant={pathname === '/' ? 'secondary' : 'ghost'}
                className={`rounded-full ${
                  pathname === '/' ? 'bg-white shadow-sm' : 'hover:bg-white'
                }`}
              >
                Dashboard
              </Button>
            </Link>

            <Link to="/package-tracking">
              <Button
                variant={pathname === '/package-tracking' ? 'secondary' : 'ghost'}
                className={`rounded-full ${
                  pathname === '/package-tracking' ? 'bg-white shadow-sm' : 'hover:bg-white'
                }`}
              >
                Package Tracking
              </Button>
            </Link>
            {/* <Link to="/simulation-control">
              <Button
                variant={pathname === '/simulation-control' ? 'secondary' : 'ghost'}
                className={`rounded-full ${
                  pathname === '/simulation-control' ? 'bg-white shadow-sm' : 'hover:bg-white'
                }`}
              >
                Simulation Control
              </Button>
            </Link>
            <Link to="/simulation-analysis">
              <Button
                variant={pathname === '/simulation-analysis' ? 'secondary' : 'ghost'}
                className={`rounded-full ${
                  pathname === '/simulation-analysis' ? 'bg-white shadow-sm' : 'hover:bg-white'
                }`}
              >
                Simulation Analysis
              </Button>
            </Link> */}

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  className="rounded-full flex items-center gap-1 hover:bg-white"
                >
                  <span>Simulation</span>
                  <ChevronDown className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                align="center"
                className="bg-white mt-1 w-48 rounded-lg shadow-md"
              >
                <Link to="/simulation-control">
                  <DropdownMenuItem className="focus:bg-gray-100 hover:bg-gray-100 cursor-pointer">
                    Simulation Control
                  </DropdownMenuItem>
                </Link>
                <Link to="/simulation-analysis">
                  <DropdownMenuItem className="focus:bg-gray-100 hover:bg-gray-100 cursor-pointer">
                    Simulation Analysis
                  </DropdownMenuItem>
                </Link>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        {/* User-Dropdown */}
        <div className="flex items-center gap-3">
          {/* Simulation Running Badge */}
          {simulationRunning && (
            <Badge
              variant="default"
              className="bg-green-600 hover:bg-green-700 text-white flex items-center gap-1 animate-pulse"
            >
              <Activity className="h-3 w-3" />
              <span>Simulation Running</span>
            </Badge>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
